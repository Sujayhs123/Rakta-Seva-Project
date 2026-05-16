import { initializeApp } from "firebase-admin/app";
import { getFirestore, FieldValue, Timestamp } from "firebase-admin/firestore";
import { getMessaging, MulticastMessage } from "firebase-admin/messaging";
import { HttpsError, onCall } from "firebase-functions/v2/https";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { logger } from "firebase-functions";

initializeApp();

const db = getFirestore();
const messaging = getMessaging();

const MATCH_RADIUS_KM = 10;
const DONATION_COOLDOWN_DAYS = 90;
const REQUEST_EXPIRY_HOURS = 24;
const MAX_MULTICAST_TOKENS = 500;

type Location = {
  lat: number;
  lng: number;
  geohash?: string;
};

type BloodRequest = {
  requestId?: string;
  requesterId: string;
  patientName: string;
  bloodGroup: string;
  unitsRequired: number;
  urgency: "critical" | "urgent" | "24hrs";
  hospitalName: string;
  hospitalAddress: string;
  location: Location;
  contactNumber: string;
  notes?: string;
  status: "active" | "fulfilled" | "expired" | "cancelled";
  notifiedDonors?: string[];
  acceptedDonors?: string[];
  createdAt?: Timestamp;
  expiresAt?: Timestamp;
};

type UserProfile = {
  uid: string;
  name: string;
  phone: string;
  bloodGroup: string;
  location?: Location;
  isAvailable: boolean;
  lastDonationDate?: Timestamp;
  fcmToken?: string;
  notificationEnabled?: boolean;
};

export const notifyMatchingDonors = onDocumentCreated(
  {
    document: "bloodRequests/{requestId}",
    region: "asia-south1"
  },
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) return;

    const requestId = event.params.requestId;
    const request = snapshot.data() as BloodRequest;

    if (request.status !== "active") {
      logger.info("Skipping non-active request", { requestId, status: request.status });
      return;
    }

    validateRequestForMatching(request);

    const expiresAt = Timestamp.fromMillis(Date.now() + REQUEST_EXPIRY_HOURS * 60 * 60 * 1000);
    const donors = await findEligibleDonors(request);
    const tokens = donors.map((donor) => donor.fcmToken).filter(Boolean) as string[];
    const notifiedDonorIds = donors.map((donor) => donor.uid);

    await snapshot.ref.update({
      requestId,
      expiresAt,
      notifiedDonors: notifiedDonorIds,
      notifiedCount: notifiedDonorIds.length,
      acceptedDonors: request.acceptedDonors ?? [],
      updatedAt: FieldValue.serverTimestamp()
    });

    if (tokens.length === 0) {
      logger.info("No eligible donor tokens found", { requestId });
      return;
    }

    await sendEmergencyNotifications(tokens, requestId, request);
  }
);

export const respondToBloodRequest = onCall(
  { region: "asia-south1" },
  async (request) => {
    const donorId = request.auth?.uid;
    if (!donorId) {
      throw new HttpsError("unauthenticated", "You must be logged in to respond.");
    }

    const requestId = stringValue(request.data.requestId, "requestId");
    const status = stringValue(request.data.status, "status");
    if (status !== "accepted" && status !== "declined") {
      throw new HttpsError("invalid-argument", "status must be accepted or declined.");
    }

    const requestRef = db.collection("bloodRequests").doc(requestId);
    const donorRef = db.collection("users").doc(donorId);
    const responseRef = requestRef.collection("responses").doc(donorId);

    return db.runTransaction(async (transaction) => {
      const [requestSnap, donorSnap] = await Promise.all([
        transaction.get(requestRef),
        transaction.get(donorRef)
      ]);

      if (!requestSnap.exists) {
        throw new HttpsError("not-found", "Blood request not found.");
      }
      if (!donorSnap.exists) {
        throw new HttpsError("failed-precondition", "Donor profile not found.");
      }

      const bloodRequest = requestSnap.data() as BloodRequest;
      const donor = donorSnap.data() as UserProfile;

      if (bloodRequest.status !== "active") {
        throw new HttpsError("failed-precondition", "This request is no longer active.");
      }
      if (!donorCanReceiveRequest(donor, bloodRequest)) {
        throw new HttpsError("permission-denied", "This donor is not eligible for the request.");
      }

      transaction.set(responseRef, {
        donorId,
        status,
        donorName: donor.name,
        donorBloodGroup: donor.bloodGroup,
        donorPhone: status === "accepted" ? donor.phone : null,
        createdAt: FieldValue.serverTimestamp()
      }, { merge: true });

      if (status === "accepted") {
        transaction.update(requestRef, {
          acceptedDonors: FieldValue.arrayUnion(donorId),
          acceptedCount: FieldValue.increment(1),
          updatedAt: FieldValue.serverTimestamp()
        });

        return {
          contactNumber: bloodRequest.contactNumber,
          hospitalName: bloodRequest.hospitalName,
          hospitalAddress: bloodRequest.hospitalAddress
        };
      }

      return { declined: true };
    });
  }
);

export const getIncomingRequest = onCall(
  { region: "asia-south1" },
  async (request) => {
    const donorId = request.auth?.uid;
    if (!donorId) {
      throw new HttpsError("unauthenticated", "You must be logged in to view a request.");
    }

    const requestId = stringValue(request.data.requestId, "requestId");
    const [requestSnap, donorSnap] = await Promise.all([
      db.collection("bloodRequests").doc(requestId).get(),
      db.collection("users").doc(donorId).get()
    ]);

    if (!requestSnap.exists) {
      throw new HttpsError("not-found", "Blood request not found.");
    }
    if (!donorSnap.exists) {
      throw new HttpsError("failed-precondition", "Donor profile not found.");
    }

    const bloodRequest = requestSnap.data() as BloodRequest;
    const donor = donorSnap.data() as UserProfile;
    if (!donorCanReceiveRequest(donor, bloodRequest)) {
      throw new HttpsError("permission-denied", "This donor is not eligible for the request.");
    }

    return {
      requestId,
      patientName: bloodRequest.patientName,
      bloodGroup: bloodRequest.bloodGroup,
      unitsRequired: bloodRequest.unitsRequired,
      urgency: bloodRequest.urgency,
      hospitalName: bloodRequest.hospitalName,
      hospitalAddress: bloodRequest.hospitalAddress,
      notes: bloodRequest.notes ?? "",
      distanceKm: donor.location ? round(distanceKm(bloodRequest.location, donor.location), 1) : null,
      createdAt: bloodRequest.createdAt?.toMillis() ?? null,
      expiresAt: bloodRequest.expiresAt?.toMillis() ?? null
    };
  }
);

export const expireOldBloodRequests = onSchedule(
  {
    schedule: "every 60 minutes",
    region: "asia-south1",
    timeZone: "Asia/Kolkata"
  },
  async () => {
    const now = Timestamp.now();
    const expired = await db.collection("bloodRequests")
      .where("status", "==", "active")
      .where("expiresAt", "<=", now)
      .limit(250)
      .get();

    const batch = db.batch();
    expired.docs.forEach((doc) => {
      batch.update(doc.ref, {
        status: "expired",
        updatedAt: FieldValue.serverTimestamp()
      });
    });

    await batch.commit();
    logger.info("Expired old blood requests", { count: expired.size });
  }
);

export const recordDonationCooldown = onDocumentCreated(
  {
    document: "donations/{donationId}",
    region: "asia-south1"
  },
  async (event) => {
    const donation = event.data?.data();
    const donorId = donation?.donorId;
    if (!donorId || typeof donorId !== "string") return;

    await db.collection("users").doc(donorId).update({
      isAvailable: false,
      isAutoCoolingDown: true,
      lastDonationDate: donation.date ?? FieldValue.serverTimestamp(),
      totalDonations: FieldValue.increment(1),
      updatedAt: FieldValue.serverTimestamp()
    });
  }
);

export const refreshDonorAvailability = onSchedule(
  {
    schedule: "every day 03:00",
    region: "asia-south1",
    timeZone: "Asia/Kolkata"
  },
  async () => {
    const cutoff = Timestamp.fromMillis(Date.now() - DONATION_COOLDOWN_DAYS * 24 * 60 * 60 * 1000);
    const readyDonors = await db.collection("users")
      .where("isAutoCoolingDown", "==", true)
      .where("lastDonationDate", "<=", cutoff)
      .limit(250)
      .get();

    const batch = db.batch();
    readyDonors.docs.forEach((doc) => {
      batch.update(doc.ref, {
        isAvailable: true,
        isAutoCoolingDown: false,
        updatedAt: FieldValue.serverTimestamp()
      });
    });

    await batch.commit();
    logger.info("Re-enabled cooled down donors", { count: readyDonors.size });
  }
);

async function findEligibleDonors(request: BloodRequest): Promise<UserProfile[]> {
  const cooldownCutoff = Timestamp.fromMillis(Date.now() - DONATION_COOLDOWN_DAYS * 24 * 60 * 60 * 1000);
  const donorSnapshot = await db.collection("users")
    .where("bloodGroup", "==", request.bloodGroup)
    .where("isAvailable", "==", true)
    .limit(500)
    .get();

  return donorSnapshot.docs
    .map((doc) => ({ uid: doc.id, ...doc.data() }) as UserProfile)
    .filter((donor) => donor.uid !== request.requesterId)
    .filter((donor) => donor.notificationEnabled !== false)
    .filter((donor) => donor.fcmToken)
    .filter((donor) => !donor.lastDonationDate || donor.lastDonationDate.toMillis() <= cooldownCutoff.toMillis())
    .filter((donor) => Boolean(donor.location))
    .filter((donor) => distanceKm(request.location, donor.location as Location) <= MATCH_RADIUS_KM);
}

async function sendEmergencyNotifications(tokens: string[], requestId: string, request: BloodRequest): Promise<void> {
  const chunks = chunk(tokens, MAX_MULTICAST_TOKENS);

  for (const tokenChunk of chunks) {
    const message: MulticastMessage = {
      tokens: tokenChunk,
      android: {
        priority: "high",
        notification: {
          channelId: "emergency_blood",
          priority: "max",
          sound: "default"
        }
      },
      notification: {
        title: "Emergency blood needed",
        body: `${request.bloodGroup} blood required at ${request.hospitalName}`
      },
      data: {
        type: "blood_request",
        requestId,
        bloodGroup: request.bloodGroup,
        urgency: request.urgency,
        hospitalName: request.hospitalName
      }
    };

    const result = await messaging.sendEachForMulticast(message);
    logger.info("Sent emergency donor notifications", {
      requestId,
      successCount: result.successCount,
      failureCount: result.failureCount
    });
  }
}

function donorCanReceiveRequest(donor: UserProfile, request: BloodRequest): boolean {
  if (donor.bloodGroup !== request.bloodGroup) return false;
  if (!donor.isAvailable) return false;
  if (!donor.location) return false;
  if (distanceKm(request.location, donor.location) > MATCH_RADIUS_KM) return false;

  const lastDonation = donor.lastDonationDate?.toMillis();
  if (!lastDonation) return true;

  const elapsedDays = (Date.now() - lastDonation) / (24 * 60 * 60 * 1000);
  return elapsedDays >= DONATION_COOLDOWN_DAYS;
}

function validateRequestForMatching(request: BloodRequest): void {
  if (!request.requesterId || !request.bloodGroup || !request.location) {
    throw new Error("Blood request is missing requesterId, bloodGroup, or location.");
  }
  if (!Number.isFinite(request.location.lat) || !Number.isFinite(request.location.lng)) {
    throw new Error("Blood request location must contain numeric lat/lng.");
  }
}

function distanceKm(a: Location, b: Location): number {
  const earthRadiusKm = 6371;
  const dLat = degreesToRadians(b.lat - a.lat);
  const dLng = degreesToRadians(b.lng - a.lng);
  const lat1 = degreesToRadians(a.lat);
  const lat2 = degreesToRadians(b.lat);

  const haversine = Math.sin(dLat / 2) ** 2
    + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) ** 2;

  return 2 * earthRadiusKm * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
}

function degreesToRadians(value: number): number {
  return value * Math.PI / 180;
}

function round(value: number, decimals: number): number {
  const factor = 10 ** decimals;
  return Math.round(value * factor) / factor;
}

function chunk<T>(items: T[], size: number): T[][] {
  const chunks: T[][] = [];
  for (let index = 0; index < items.length; index += size) {
    chunks.push(items.slice(index, index + size));
  }
  return chunks;
}

function stringValue(value: unknown, fieldName: string): string {
  if (typeof value !== "string" || value.trim().length === 0) {
    throw new HttpsError("invalid-argument", `${fieldName} must be a non-empty string.`);
  }
  return value.trim();
}

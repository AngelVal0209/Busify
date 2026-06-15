const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

const db = admin.firestore();

/**
 * Sends push notification when a new ticket is purchased
 */
exports.onTicketCreated = functions.firestore
    .document("tickets/{ticketId}")
    .onCreate(async (snap, context) => {
        const ticket = snap.data();
        const userId = ticket.userId;

        try {
            const userDoc = await db.collection("users").doc(userId).get();
            const user = userDoc.data();
            const token = user?.fcmToken;
            if (!token) return;

            const message = {
                notification: {
                    title: "Ticket Confirmado",
                    body: `Viaje ${ticket.origin} → ${ticket.destination} confirmado. Asientos: ${ticket.seatNumbers?.join(", ")}`,
                },
                token: token,
            };

            await admin.messaging().send(message);
        } catch (error) {
            console.error("Error sending ticket notification:", error);
        }
    });

/**
 * Sends push notification when a route status changes
 */
exports.onRouteUpdated = functions.firestore
    .document("routes/{routeId}")
    .onUpdate(async (change, context) => {
        const before = change.before.data();
        const after = change.after.data();

        if (before.status === after.status) return;

        try {
            const ticketsSnap = await db.collection("tickets")
                .where("routeId", context.params.routeId)
                .get();

            const tokens = [];
            for (const doc of ticketsSnap.docs) {
                const ticket = doc.data();
                const userDoc = await db.collection("users").doc(ticket.userId).get();
                const token = userDoc.data()?.fcmToken;
                if (token) tokens.push(token);
            }

            if (tokens.length === 0) return;

            const message = {
                notification: {
                    title: "Estado de Ruta Actualizado",
                    body: `La ruta ${after.origin} → ${after.destination} ahora está: ${after.status}`,
                },
                tokens: tokens,
            };

            await admin.messaging().sendEachForMulticast(message);
        } catch (error) {
            console.error("Error sending route update notification:", error);
        }
    });

/**
 * Sends push notification when a new route is created
 */
exports.onRouteCreated = functions.firestore
    .document("routes/{routeId}")
    .onCreate(async (snap, context) => {
        const route = snap.data();

        try {
            const usersSnap = await db.collection("users").get();
            const tokens = [];
            usersSnap.forEach((doc) => {
                const token = doc.data()?.fcmToken;
                if (token) tokens.push(token);
            });

            if (tokens.length === 0) return;

            const message = {
                notification: {
                    title: "Nueva Ruta Disponible",
                    body: `${route.origin} → ${route.destination} por ${route.company}. Salida: ${route.departureTime}`,
                },
                tokens: tokens,
            };

            await admin.messaging().sendEachForMulticast(message);
        } catch (error) {
            console.error("Error sending new route notification:", error);
        }
    });

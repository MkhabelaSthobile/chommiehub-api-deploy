
using System.Security.Cryptography;
using System.Text;
using ChommieHub.Api.Models;
using Google.Cloud.Firestore;

namespace ChommieHub.Api.Repositories;

/// Stores and manages Firebase Cloud Messaging device registrations in Firestore.
public sealed class FirestoreFcmDeviceRepository
{
    private const string CollectionName = "fcm_device_registrations";

    private readonly CollectionReference _collection;

    public FirestoreFcmDeviceRepository(FirestoreDb firestoreDb)
    {
        _collection = firestoreDb.Collection(CollectionName);
    }

    /// Registers an FCM token for the verified Firebase user.
    /// Re-registering the same token updates the existing document.
    public async Task RegisterDeviceAsync(
        string userId,
        string fcmToken)
    {
        if (string.IsNullOrWhiteSpace(userId))
        {
            throw new ArgumentException(
                "A verified user ID is required.",
                nameof(userId));
        }

        if (string.IsNullOrWhiteSpace(fcmToken))
        {
            throw new ArgumentException(
                "An FCM token is required.",
                nameof(fcmToken));
        }

        string documentId = GetTokenDocumentId(fcmToken);
        DocumentReference document = _collection.Document(documentId);

        await document.SetAsync(new Dictionary<string, object>
        {
            [nameof(FcmDeviceRegistration.UserId)] = userId,
            [nameof(FcmDeviceRegistration.FcmToken)] = fcmToken,
            [nameof(FcmDeviceRegistration.Platform)] = "android",
            [nameof(FcmDeviceRegistration.CreatedAt)] = FieldValue.ServerTimestamp,
            [nameof(FcmDeviceRegistration.UpdatedAt)] = FieldValue.ServerTimestamp
        }, SetOptions.MergeAll);
    }

    
    /// Removes a token only when it belongs to the authenticated Firebase user.
    public async Task<bool> RemoveDeviceAsync(
        string userId,
        string fcmToken)
    {
        if (string.IsNullOrWhiteSpace(userId) ||
            string.IsNullOrWhiteSpace(fcmToken))
        {
            return false;
        }

        string documentId = GetTokenDocumentId(fcmToken);
        DocumentReference document = _collection.Document(documentId);

        return await _collection.Database.RunTransactionAsync(async transaction =>
        {
            DocumentSnapshot snapshot = await transaction.GetSnapshotAsync(document);

            if (!snapshot.Exists)
            {
                return false;
            }

            string storedUserId = snapshot.GetValue<string>(
                nameof(FcmDeviceRegistration.UserId));

            if (storedUserId != userId)
            {
                return false;
            }

            transaction.Delete(document);
            return true;
        });
    }


    /// Retrieves all registered FCM devices for a user.
    /// This method must only be called by trusted backend services.
    public async Task<IReadOnlyList<FcmDeviceRegistration>> GetDevicesForUserAsync(
        string userId)
    {
        if (string.IsNullOrWhiteSpace(userId))
        {
            return Array.Empty<FcmDeviceRegistration>();
        }

        QuerySnapshot snapshot = await _collection
            .WhereEqualTo(nameof(FcmDeviceRegistration.UserId), userId)
            .GetSnapshotAsync();

        return snapshot.Documents
            .Select(document => document.ConvertTo<FcmDeviceRegistration>())
            .ToList();
    }


    /// Generates a stable Firestore document ID without
    /// exposing the raw FCM token in the document path.
    private static string GetTokenDocumentId(string fcmToken)
    {
        byte[] tokenBytes = Encoding.UTF8.GetBytes(fcmToken);
        byte[] hash = SHA256.HashData(tokenBytes);

        return Convert.ToHexString(hash).ToLowerInvariant();
    }
}


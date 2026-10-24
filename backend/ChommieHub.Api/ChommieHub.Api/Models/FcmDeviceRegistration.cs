
using Google.Cloud.Firestore;

namespace ChommieHub.Api.Models;


/// Represents an Android device registered to receive
/// Firebase Cloud Messaging notifications.
[FirestoreData]
public sealed class FcmDeviceRegistration
{
   
    /// Firebase UID of the authenticated device owner.
    /// This value must be determined from a verified
    /// Firebase ID token, never from the request body. 
    [FirestoreProperty]
    public string UserId { get; set; } = string.Empty;

    /// Firebase Cloud Messaging registration token.
    /// Treat this as sensitive device-routing information.
    [FirestoreProperty]
    public string FcmToken { get; set; } = string.Empty;

    
    /// Device platform, currently Android.
    [FirestoreProperty]
    public string Platform { get; set; } = "android";

    
    /// Time the registration was first created.
    [FirestoreProperty]
    public Timestamp CreatedAt { get; set; } = Timestamp.FromDateTime(
        DateTime.UnixEpoch
    );

   
    /// Time the registration was last updated.
    [FirestoreProperty]
    public Timestamp UpdatedAt { get; set; } = Timestamp.FromDateTime(
        DateTime.UnixEpoch
    );
}

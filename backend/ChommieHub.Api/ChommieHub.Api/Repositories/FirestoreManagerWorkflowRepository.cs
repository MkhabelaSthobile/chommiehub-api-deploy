using ChommieHub.Api.Models;
using Google.Cloud.Firestore;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Coordinates Firestore operations that must succeed or fail together
    /// during residence-manager workflows.
    /// </summary>
    public sealed class FirestoreManagerWorkflowRepository : IManagerWorkflowRepository
    {
        private readonly FirestoreDb _firestore;

        public FirestoreManagerWorkflowRepository(FirestoreDb firestore)
        {
            _firestore = firestore;
        }

        public async Task<Resident?> AssignRoomAsync(string applicationId, string roomId, CancellationToken cancellationToken = default)
        {
            DocumentReference applicationReference = _firestore.Collection("applications").Document(applicationId);

            DocumentReference roomReference = _firestore.Collection("rooms").Document(roomId);

            // The application ID is used as the resident document ID.
            // This guarantees one resident record per approved application.
            DocumentReference residentReference = _firestore.Collection("residents").Document(applicationId);

            // Transaction pattern follows the official Google Cloud Firestore .NET transaction guidance.
            return await _firestore.RunTransactionAsync(async transaction =>
            { // Firestore requires transaction reads to occur before transactional writes are queued
              IList<DocumentSnapshot> snapshots = await transaction.GetAllSnapshotsAsync(
                  new[]
                  {
                      applicationReference,
                      roomReference,
                      residentReference
                  },
                  
                  fieldMask: null, cancellationToken);
                
                DocumentSnapshot applicationSnapshot = snapshots[0];

                DocumentSnapshot roomSnapshot = snapshots[1];

                DocumentSnapshot residentSnapshot = snapshots[2];

                    if (!applicationSnapshot.Exists)
                    {
                        return null;
                    }

                    Application application = applicationSnapshot.ConvertTo<Application>();

                    if (application.Status != ApplicationStatuses.Approved)
                    {
                        throw new InvalidOperationException("Only approved applications can receive a room assignment.");
                    }

                    if (!roomSnapshot.Exists)
                    {
                        throw new KeyNotFoundException("The selected room was not found.");
                    }

                    if (residentSnapshot.Exists)
                    {
                        throw new InvalidOperationException("A resident has already been created for this application.");
                    }

                    Room room = roomSnapshot.ConvertTo<Room>();

                    if (room.AccommodationId != application.AccommodationId)
                    {
                        throw new InvalidOperationException("The selected room does not belong to the application's accommodation.");
                    }

                    if (!room.Availability)
                    {
                        throw new InvalidOperationException("The selected room is not available.");
                    }

                    DateTime now = DateTime.UtcNow;

                    room.Availability = false;
                    room.UpdatedAtUtc = now;

                    application.AssignedRoomId = room.Id;

                    var resident = new Resident
                    {
                        Id = application.Id,
                        StudentId = application.StudentId,
                        AccommodationId = application.AccommodationId,
                        RoomId = room.Id,
                        ApplicationId = application.Id,
                        IsActive = true,
                        CreatedAtUtc = now
                    };

                    //these writes are committed together. If the transactionfails, none of the three documents are partially updated.
                    transaction.Set(roomReference, room, SetOptions.Overwrite);

                    transaction.Set(applicationReference, application, SetOptions.Overwrite);

                    transaction.Create(residentReference, resident);

                    return resident;
                },
                cancellationToken: cancellationToken);
        }
    }
}
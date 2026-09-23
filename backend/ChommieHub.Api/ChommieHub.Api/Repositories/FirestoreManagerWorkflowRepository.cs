using ChommieHub.Api.Models;
using Google.Cloud.Firestore;
using ApplicationModel = ChommieHub.Api.Models.Application;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Coordinates Firestore operations that must succeed or fail together when a residence manager assigns an approved applicant to a room.
    ///
    /// The transaction updates the room, application and student profile while also creating the resident record. This ensures that the Applicant-to-Resident transition remains
    /// consistent across all related Firestore documents.
    /// </summary>
    public sealed class FirestoreManagerWorkflowRepository : IManagerWorkflowRepository
    {
        private readonly FirestoreDb _firestore;

        public FirestoreManagerWorkflowRepository(FirestoreDb firestore)
        {
            _firestore = firestore;
        }

        /// <summary>
        /// Assigns an available room to an approved application and converts the linked student account from Applicant to Resident.
        /// </summary>
        public async Task<Resident?> AssignRoomAsync(string applicationId, string roomId, CancellationToken cancellationToken = default)
        {
            DocumentReference applicationReference = _firestore.Collection("applications").Document(applicationId);
            DocumentReference roomReference = _firestore.Collection("rooms").Document(roomId);
            DocumentReference residentReference = _firestore.Collection("residents").Document(applicationId);

            return await _firestore.RunTransactionAsync(async transaction =>
            {
                DocumentSnapshot applicationSnapshot = await transaction.GetSnapshotAsync(applicationReference, cancellationToken);

                if (!applicationSnapshot.Exists)
                {
                    return null;
                }

                ApplicationModel application = applicationSnapshot.ConvertTo<ApplicationModel>();

                if (application.Status != ApplicationStatuses.Approved)
                {
                    throw new InvalidOperationException("Only approved applications can receive a room assignment.");
                }

                DocumentSnapshot roomSnapshot = await transaction.GetSnapshotAsync(roomReference, cancellationToken);

                if (!roomSnapshot.Exists)
                {
                    throw new KeyNotFoundException("The selected room was not found.");
                }

                DocumentSnapshot residentSnapshot = await transaction.GetSnapshotAsync(residentReference, cancellationToken);

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

                DocumentReference studentReference = _firestore.Collection("students").Document(application.StudentId);
                DocumentSnapshot studentSnapshot = await transaction.GetSnapshotAsync(studentReference, cancellationToken);

                if (!studentSnapshot.Exists)
                {
                    throw new KeyNotFoundException("The student profile linked to this application was not found.");
                }

                Student student = studentSnapshot.ConvertTo<Student>();
                DateTime now = DateTime.UtcNow;

                room.Availability = false;
                room.UpdatedAtUtc = now;

                application.AssignedRoomId = room.Id;

                student.UserType = "Resident";
                student.AccommodationId = application.AccommodationId;
                student.RoomId = room.Id;
                student.UpdatedAtUtc = now;

                Resident resident = new()
                {
                    Id = application.Id,
                    StudentId = application.StudentId,
                    AccommodationId = application.AccommodationId,
                    RoomId = room.Id,
                    ApplicationId = application.Id,
                    IsActive = true,
                    CreatedAtUtc = now
                };

                transaction.Set(roomReference, room, SetOptions.Overwrite);
                transaction.Set(applicationReference, application, SetOptions.Overwrite);
                transaction.Set(studentReference, student, SetOptions.Overwrite);
                transaction.Create(residentReference, resident);

                return resident;
            }, cancellationToken: cancellationToken);
        }
    }
}
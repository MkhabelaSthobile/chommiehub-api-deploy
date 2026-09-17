using ChommieHub.Api.Models;
using Google.Cloud.Firestore;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Firestore implementation used to persist Chommiehub student profiles
    /// </summary>
    public sealed class FirestoreStudentRepository : IStudentRepository
    {
        private const string CollectionName = "students";

        private readonly FirestoreDb _firestore;

        public FirestoreStudentRepository(FirestoreDb firestore)
        {
            _firestore = firestore;
        }

        public async Task<Student?> GetByIdAsync(string id, CancellationToken cancellationToken = default)
        {
            DocumentSnapshot snapshot = await _firestore
                .Collection(CollectionName)
                .Document(id)
                .GetSnapshotAsync(cancellationToken);

            if (!snapshot.Exists)
            {
                return null;
            }

            return snapshot.ConvertTo<Student>();
        }

        /// <summary>
        /// Creates the backend student profile using the Firebase Authentication UID as the Firestore document identifier. 
        /// This keeps authentication and application data linked through one stable student ID
        /// </summary>
        public async Task<Student> CreateAsync(
            Student student,
            CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(student.FirebaseUid))
            {
                throw new ArgumentException(
                    "A Firebase UID is required to create a student profile.",
                    nameof(student));
            }

            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document(student.FirebaseUid);

            student.Id = student.FirebaseUid;
            student.CreatedAtUtc = DateTime.UtcNow;
            student.UpdatedAtUtc = DateTime.UtcNow;

            await documentReference.SetAsync(
                student,
                cancellationToken: cancellationToken);

            return student;
        }

        public async Task<bool> UpdateAsync(Student student, CancellationToken cancellationToken = default)
        {
            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document(student.Id);

            DocumentSnapshot snapshot = await documentReference.GetSnapshotAsync(cancellationToken);

            if (!snapshot.Exists)
            {
                return false;
            }

            await documentReference.SetAsync(
                student,
                SetOptions.Overwrite,
                cancellationToken);

            return true;
        }
    }
}
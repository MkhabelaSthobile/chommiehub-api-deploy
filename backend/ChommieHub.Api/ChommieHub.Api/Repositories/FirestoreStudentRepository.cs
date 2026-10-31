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

            // Preserve the identity contract:
            // Firebase UID = Student.Id = Firestore document ID.
            student.Id = student.FirebaseUid;

            DateTime now = DateTime.UtcNow;
            student.CreatedAtUtc = now;
            student.UpdatedAtUtc = now;

            // CreateAsync fails if the document already exists.
            // Unlike SetAsync, it will not overwrite an existing profile.
            await documentReference.CreateAsync(
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

        public async Task<IReadOnlyList<Student>> GetAllAsync(CancellationToken cancellationToken = default)
        {
            QuerySnapshot snapshot = await _firestore
                .Collection(CollectionName)
                .GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document => document.ConvertTo<Student>())
                .ToList();
        }
    }
}
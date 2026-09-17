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

        public async Task<Student> CreateAsync(Student student, CancellationToken cancellationToken = default)
        {
            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document();

            student.Id = documentReference.Id;

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
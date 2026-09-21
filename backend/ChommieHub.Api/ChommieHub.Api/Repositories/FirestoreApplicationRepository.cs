using ChommieHub.Api.Models;
using Google.Cloud.Firestore;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Firestore repository for student accommodation applications.
    ///
    /// Student submission and manager review both operate on the same
    /// "applications" collection.
    /// </summary>
    public sealed class FirestoreApplicationRepository : IApplicationRepository
    {
        private const string CollectionName = "applications";

        private readonly FirestoreDb _firestore;

        public FirestoreApplicationRepository(FirestoreDb firestore)
        {
            _firestore = firestore;
        }

        /// <summary>
        /// Creates a new student accommodation application.
        /// </summary>
        public async Task<Application> CreateAsync(
            Application application,
            CancellationToken cancellationToken = default)
        {
            CollectionReference collection =
                _firestore.Collection(CollectionName);

            // Let Firestore generate the application document ID.
            DocumentReference documentReference =
                collection.Document();

            application.Id = documentReference.Id;

            // Make sure a new application always has a submission date.
            if (application.DateSubmittedUtc == default)
            {
                application.DateSubmittedUtc = DateTime.UtcNow;
            }

            await documentReference.SetAsync(
                application,
                cancellationToken: cancellationToken);

            return application;
        }

        /// <summary>
        /// Returns all applications submitted by a particular student.
        /// </summary>
        public async Task<IReadOnlyList<Application>> GetByStudentIdAsync(
            string studentId,
            CancellationToken cancellationToken = default)
        {
            Query query = _firestore
                .Collection(CollectionName)
                .WhereEqualTo("StudentId", studentId);

            QuerySnapshot snapshot =
                await query.GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document =>
                    document.ConvertTo<Application>())
                .OrderByDescending(application =>
                    application.DateSubmittedUtc)
                .ToList();
        }

        /// <summary>
        /// Returns applications submitted to a particular accommodation.
        /// This is used by the manager workflow.
        /// </summary>
        public async Task<IReadOnlyList<Application>> GetByAccommodationIdAsync(
            string accommodationId,
            CancellationToken cancellationToken = default)
        {
            Query query = _firestore
                .Collection(CollectionName)
                .WhereEqualTo(
                    "AccommodationId",
                    accommodationId);

            QuerySnapshot snapshot =
                await query.GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document =>
                    document.ConvertTo<Application>())
                .OrderByDescending(application =>
                    application.DateSubmittedUtc)
                .ToList();
        }

        /// <summary>
        /// Returns one application using its Firestore document ID.
        /// </summary>
        public async Task<Application?> GetByIdAsync(
            string id,
            CancellationToken cancellationToken = default)
        {
            DocumentSnapshot snapshot = await _firestore
                .Collection(CollectionName)
                .Document(id)
                .GetSnapshotAsync(cancellationToken);

            if (!snapshot.Exists)
            {
                return null;
            }

            return snapshot.ConvertTo<Application>();
        }

        /// <summary>
        /// Updates an existing application.
        ///
        /// This allows manager decisions such as Approved or Declined
        /// to update the same Firestore application originally created
        /// by the student.
        /// </summary>
        public async Task<bool> UpdateAsync(
            Application application,
            CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(application.Id))
            {
                return false;
            }

            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document(application.Id);

            DocumentSnapshot snapshot =
                await documentReference.GetSnapshotAsync(
                    cancellationToken);

            if (!snapshot.Exists)
            {
                return false;
            }

            await documentReference.SetAsync(
                application,
                SetOptions.Overwrite,
                cancellationToken);

            return true;
        }
    }
}
namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Provides detailed application information used during manager review
    /// </summary>
    public sealed class ManagerApplicationDetailResponse
    {
        public string Id { get; init; } = string.Empty;

        public string StudentId { get; init; } = string.Empty;

        public string AccommodationId { get; init; } = string.Empty;

        public string RoomTypePreference { get; init; } = string.Empty;

        public string RentalPeriod { get; init; } = string.Empty;

        public string SupportingInfo { get; init; } = string.Empty;

        public string Status { get; init; } = string.Empty;

        public string AssignedRoomId { get; init; } = string.Empty;

        public string ReviewNotes { get; init; } = string.Empty;

        public DateTime DateSubmittedUtc { get; init; }

        public DateTime? DateReviewedUtc { get; init; }
    }
}
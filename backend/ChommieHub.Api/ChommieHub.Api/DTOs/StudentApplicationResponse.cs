namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Represents one accommodation application belonging to the signed-in student.
    /// </summary>
    public sealed class StudentApplicationResponse
    {
        public string Id { get; init; } = string.Empty;
        public string AccommodationId { get; init; } = string.Empty;
        public string AccommodationName { get; init; } = string.Empty;
        public string RequestedRoomId { get; init; } = string.Empty;
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
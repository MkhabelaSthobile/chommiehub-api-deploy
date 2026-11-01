namespace ChommieHub.Api.DTOs
{
    // The manager's own accommodation, including its review status.
    public sealed class ManagerAccommodationResponse
    {
        public string Id { get; init; } = string.Empty;

        public string Name { get; init; } = string.Empty;

        public string Address { get; init; } = string.Empty;

        public double Latitude { get; init; }

        public double Longitude { get; init; }

        public string Description { get; init; } = string.Empty;

        public string PhoneNumber { get; init; } = string.Empty;

        public List<string> Amenities { get; init; } = [];

        public string InstitutionArea { get; init; } = string.Empty;

        public bool IsActive { get; init; }

        public string ReviewStatus { get; init; } = string.Empty;

        public string ReviewReason { get; init; } = string.Empty;

        public DateTime? SubmittedAtUtc { get; init; }

        public DateTime? ReviewedAtUtc { get; init; }

        public bool IsArchived { get; init; }

        public List<string> ImageUrls { get; init; } = [];
    }
}
namespace ChommieHub.Api.DTOs
{
    /// <summary>
    ///Represents accommodation data returned to API clients.
    /// </summary>
    public sealed class AccommodationResponse
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
    }
}
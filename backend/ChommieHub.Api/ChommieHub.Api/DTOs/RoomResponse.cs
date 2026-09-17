namespace ChommieHub.Api.DTOs
{
    /// <summary>
    ///represents room data returned to API clients.
    /// </summary>
    public sealed class RoomResponse
    {
        public string Id { get; init; } = string.Empty;

        public string AccommodationId { get; init; } = string.Empty;

        public string RoomType { get; init; } = string.Empty;

        public double Price { get; init; }

        public int Capacity { get; init; }

        public bool Availability { get; init; }
    }
}
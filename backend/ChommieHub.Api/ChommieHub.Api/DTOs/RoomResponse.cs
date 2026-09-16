namespace ChommieHub.Api.DTOs
{
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
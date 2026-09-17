namespace ChommieHub.Api.DTOs
{
    public sealed class ManagerDashboardResponse
    {
        public string AccommodationId { get; init; } = string.Empty;

        public int TotalApplications { get; init; }

        public int PendingApplications { get; init; }

        public int ApprovedApplications { get; init; }

        public int DeclinedApplications { get; init; }

        public int ActiveResidents { get; init; }

        public int TotalRooms { get; init; }

        public int AvailableRooms { get; init; }

        public int UnavailableRooms { get; init; }
    }
}
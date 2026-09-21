namespace ChommieHub.Api.Models
{
    /// <summary>
    /// Defines the valid statuses for a maintenance request.
    /// </summary>
    public static class MaintenanceStatuses
    {
        public const string Pending = "Pending";
        public const string InProgress = "In Progress";
        public const string Resolved = "Resolved";
    }
}
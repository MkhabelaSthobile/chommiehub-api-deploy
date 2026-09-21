namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Data required when a residence manager updates a maintenance request.
    /// </summary>
    public sealed class UpdateMaintenanceStatusRequest
    {
        public string Status { get; set; } = string.Empty;
    }
}
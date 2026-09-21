namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Data required when a manager reviews a visitor request.
    /// </summary>
    public sealed class UpdateVisitorRequestStatusRequest
    {
        public string Status { get; set; } = string.Empty;
    }
}
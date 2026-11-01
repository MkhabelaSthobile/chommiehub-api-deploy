using System.ComponentModel.DataAnnotations;

namespace ChommieHub.Api.DTOs
{
    // The reason an administrator gives for an action such as reject, archive,
    // disable or delete. It is checked by the service after the admin check.
    public sealed class AdminReasonRequest
    {
        [StringLength(500)]
        public string? Reason { get; set; }
    }
}

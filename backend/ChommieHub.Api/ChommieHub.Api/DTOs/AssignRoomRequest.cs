using System.ComponentModel.DataAnnotations;

namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Identifies the room selected for an approved application.
    /// </summary>
    public sealed class AssignRoomRequest
    {
        [Required]
        public string RoomId { get; set; } = string.Empty;
    }
}
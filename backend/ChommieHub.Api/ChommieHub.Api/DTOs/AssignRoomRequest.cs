using System.ComponentModel.DataAnnotations;

namespace ChommieHub.Api.DTOs
{
    public sealed class AssignRoomRequest
    {
        [Required]
        public string RoomId { get; set; } = string.Empty;
    }
}
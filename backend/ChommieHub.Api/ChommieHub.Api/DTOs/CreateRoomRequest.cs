using System.ComponentModel.DataAnnotations;

namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Defines validated input required to create a room.
    /// </summary>
    public sealed class CreateRoomRequest
    {
        [Required]
        [StringLength(100)]
        public string RoomType { get; set; } = string.Empty;

        [Range(0, 1000000)]
        public double Price { get; set; }

        [Range(1, 20)]
        public int Capacity { get; set; }

        public bool Availability { get; set; } = true;
    }
}
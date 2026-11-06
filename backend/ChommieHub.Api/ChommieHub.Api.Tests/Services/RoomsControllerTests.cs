using ChommieHub.Api.Controllers;
using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging.Abstractions;
using Moq;

namespace ChommieHub.Api.Tests.Controllers;

/// <summary>
/// Public room details are visible only when their accommodation is approved.
/// </summary>
public sealed class RoomsControllerTests
{
    private readonly Mock<IRoomService> _rooms = new();
    private readonly Mock<IAccommodationService> _accommodations = new();

    private RoomsController CreateController() => new(
        _rooms.Object,
        _accommodations.Object,
        null!, // Only the public GET action is exercised by these tests.
        null!,
        NullLogger<RoomsController>.Instance);

    [Fact]
    public async Task GetById_WithUnapprovedAccommodation_ReturnsNotFound()
    {
        _rooms.Setup(service => service.GetByIdAsync("r1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(new RoomResponse { Id = "r1", AccommodationId = "a1" });
        _accommodations.Setup(service => service.GetByIdAsync("a1", It.IsAny<CancellationToken>()))
            .ReturnsAsync((AccommodationResponse?)null);

        var result = await CreateController().GetById("r1", CancellationToken.None);

        Assert.IsType<NotFoundObjectResult>(result.Result);
    }

    [Fact]
    public async Task GetById_WithApprovedAccommodation_ReturnsRoom()
    {
        _rooms.Setup(service => service.GetByIdAsync("r1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(new RoomResponse { Id = "r1", AccommodationId = "a1" });
        _accommodations.Setup(service => service.GetByIdAsync("a1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(new AccommodationResponse { Id = "a1" });

        var result = await CreateController().GetById("r1", CancellationToken.None);

        Assert.IsType<OkObjectResult>(result.Result);
    }
}

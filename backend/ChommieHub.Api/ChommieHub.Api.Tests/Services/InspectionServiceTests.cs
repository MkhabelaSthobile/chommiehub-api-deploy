using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using Moq;

namespace ChommieHub.Api.Tests.Services;

/// <summary>
/// Verifies digital-inspection business rules without requiring a live Firestore connection.
/// </summary>
public sealed class InspectionServiceTests
{
    private readonly Mock<IInspectionRepository> _repository = new();

    private InspectionService CreateService()
    {
        return new InspectionService(_repository.Object);
    }

    [Fact]
    public async Task CreateAsync_CreatesMoveInInspection()
    {
        _repository.Setup(repository => repository.CreateAsync(It.IsAny<Inspection>(), It.IsAny<CancellationToken>())).ReturnsAsync((Inspection inspection, CancellationToken _) =>
        {
            inspection.Id = "inspection-1";
            return inspection;
        });

        CreateInspectionRequest request = new()
        {
            ResidentId = "resident-1",
            StudentId = "student-1",
            AccommodationId = "accommodation-1",
            RoomId = "room-1",
            Type = "Move In",
            Items =
            [
                new InspectionItemRequest
                {
                    ItemName = "Bed",
                    Condition = "Good",
                    Comments = "No visible damage."
                }
            ]
        };

        InspectionResponse result = await CreateService().CreateAsync(request);

        Assert.Equal("inspection-1", result.Id);
        Assert.Equal("Move-In", result.Type);
        Assert.Single(result.Items);
        Assert.Equal("Bed", result.Items[0].ItemName);
        Assert.Equal("Submitted", result.Status);
    }

    [Fact]
    public async Task CreateAsync_RejectsInspectionWithoutItems()
    {
        CreateInspectionRequest request = new()
        {
            ResidentId = "resident-1",
            StudentId = "student-1",
            AccommodationId = "accommodation-1",
            RoomId = "room-1",
            Type = "Move-Out",
            Items = []
        };

        InvalidOperationException exception = await Assert.ThrowsAsync<InvalidOperationException>(() => CreateService().CreateAsync(request));

        Assert.Equal("At least one inspection item is required.", exception.Message);
        _repository.Verify(repository => repository.CreateAsync(It.IsAny<Inspection>(), It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task CreateAsync_RejectsInvalidInspectionType()
    {
        CreateInspectionRequest request = new()
        {
            ResidentId = "resident-1",
            StudentId = "student-1",
            AccommodationId = "accommodation-1",
            RoomId = "room-1",
            Type = "Routine",
            Items = [new InspectionItemRequest { ItemName = "Door", Condition = "Good" }]
        };

        InvalidOperationException exception = await Assert.ThrowsAsync<InvalidOperationException>(() => CreateService().CreateAsync(request));

        Assert.Equal("Inspection type must be Move-In or Move-Out.", exception.Message);
    }

    [Fact]
    public async Task ConfirmAsync_ReturnsConfirmedInspection()
    {
        Inspection confirmedInspection = new()
        {
            Id = "inspection-1",
            ResidentId = "resident-1",
            StudentId = "student-1",
            AccommodationId = "accommodation-1",
            RoomId = "room-1",
            Type = "Move-In",
            Status = "Confirmed",
            ManagerConfirmed = true,
            ManagerNotes = "Checked with resident.",
            ConfirmedAtUtc = DateTime.UtcNow
        };

        _repository.Setup(repository => repository.ConfirmAsync("inspection-1", "Checked with resident.", It.IsAny<CancellationToken>())).ReturnsAsync(confirmedInspection);

        InspectionResponse? result = await CreateService().ConfirmAsync("inspection-1", new ConfirmInspectionRequest { ManagerNotes = "Checked with resident." });

        Assert.NotNull(result);
        Assert.True(result.ManagerConfirmed);
        Assert.Equal("Confirmed", result.Status);
        Assert.Equal("Checked with resident.", result.ManagerNotes);
    }
}
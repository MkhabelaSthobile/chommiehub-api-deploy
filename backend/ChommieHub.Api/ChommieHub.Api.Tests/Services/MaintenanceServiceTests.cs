using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using Moq;

namespace ChommieHub.Api.Tests.Services;

/// <summary>
/// Verifies maintenance-request business rules without requiring a live Firestore connection.
/// </summary>
public sealed class MaintenanceServiceTests
{
    private readonly Mock<IMaintenanceRepository> _maintenanceRepository = new();

    private MaintenanceService CreateService()
    {
        return new MaintenanceService(_maintenanceRepository.Object);
    }

    [Fact]
    public async Task CreateAsync_CreatesValidMaintenanceRequest()
    {
        // Arrange
        _maintenanceRepository
            .Setup(repository => repository.CreateAsync(
                It.IsAny<MaintenanceRequest>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync((MaintenanceRequest request, CancellationToken _) =>
            {
                request.Id = "maintenance-1";
                return request;
            });

        MaintenanceService service = CreateService();

        var request = new CreateMaintenanceRequest
        {
            ResidentId = "resident-1",
            AccommodationId = "accommodation-1",
            RoomId = "room-1",
            Category = "Plumbing",
            Description = "The bathroom tap is leaking."
        };

        // Act
        MaintenanceRequestResponse result =
            await service.CreateAsync(request);

        // Assert
        Assert.Equal("maintenance-1", result.Id);
        Assert.Equal("resident-1", result.ResidentId);
        Assert.Equal("Plumbing", result.Category);
        Assert.Equal(
            "The bathroom tap is leaking.",
            result.Description);
        Assert.Equal(MaintenanceStatuses.Pending, result.Status);

        _maintenanceRepository.Verify(
            repository => repository.CreateAsync(
                It.Is<MaintenanceRequest>(maintenance =>
                    maintenance.ResidentId == "resident-1" &&
                    maintenance.RoomId == "room-1" &&
                    maintenance.Status == MaintenanceStatuses.Pending),
                It.IsAny<CancellationToken>()),
            Times.Once);
    }

    [Fact]
    public async Task CreateAsync_RejectsMissingDescription()
    {
        // Arrange
        MaintenanceService service = CreateService();

        var request = new CreateMaintenanceRequest
        {
            ResidentId = "resident-1",
            AccommodationId = "accommodation-1",
            RoomId = "room-1",
            Category = "Electrical",
            Description = string.Empty
        };

        // Act + Assert
        InvalidOperationException exception =
            await Assert.ThrowsAsync<InvalidOperationException>(
                () => service.CreateAsync(request));

        Assert.Equal(
            "Maintenance description is required.",
            exception.Message);

        _maintenanceRepository.Verify(
            repository => repository.CreateAsync(
                It.IsAny<MaintenanceRequest>(),
                It.IsAny<CancellationToken>()),
            Times.Never);
    }

    [Fact]
    public async Task UpdateStatusAsync_UpdatesToInProgress()
    {
        // Arrange
        var maintenanceRequest = new MaintenanceRequest
        {
            Id = "maintenance-1",
            ResidentId = "resident-1",
            AccommodationId = "accommodation-1",
            RoomId = "room-1",
            Category = "Plumbing",
            Description = "The bathroom tap is leaking.",
            Status = MaintenanceStatuses.InProgress,
            CreatedAtUtc = DateTime.UtcNow,
            UpdatedAtUtc = DateTime.UtcNow
        };

        _maintenanceRepository
            .Setup(repository => repository.UpdateStatusAsync(
                "maintenance-1",
                MaintenanceStatuses.InProgress,
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(maintenanceRequest);

        MaintenanceService service = CreateService();

        var request = new UpdateMaintenanceStatusRequest
        {
            Status = "in progress"
        };

        // Act
        MaintenanceRequestResponse? result =
            await service.UpdateStatusAsync(
                "maintenance-1",
                request);

        // Assert
        Assert.NotNull(result);
        Assert.Equal(MaintenanceStatuses.InProgress, result.Status);

        _maintenanceRepository.Verify(
            repository => repository.UpdateStatusAsync(
                "maintenance-1",
                MaintenanceStatuses.InProgress,
                It.IsAny<CancellationToken>()),
            Times.Once);
    }

    [Fact]
    public async Task UpdateStatusAsync_RejectsInvalidStatus()
    {
        // Arrange
        MaintenanceService service = CreateService();

        var request = new UpdateMaintenanceStatusRequest
        {
            Status = "Cancelled"
        };

        // Act + Assert
        InvalidOperationException exception =
            await Assert.ThrowsAsync<InvalidOperationException>(
                () => service.UpdateStatusAsync(
                    "maintenance-1",
                    request));

        Assert.Equal(
            "Status must be Pending, In Progress or Resolved.",
            exception.Message);

        _maintenanceRepository.Verify(
            repository => repository.UpdateStatusAsync(
                It.IsAny<string>(),
                It.IsAny<string>(),
                It.IsAny<CancellationToken>()),
            Times.Never);
    }
}
using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using Moq;

namespace ChommieHub.Api.Tests.Services;

/// <summary>
/// Verifies visitor-request business rules without requiring a live Firestore connection.
/// </summary>
public sealed class VisitorRequestServiceTests
{
    private readonly Mock<IVisitorRequestRepository> _visitorRequestRepository = new();

    private VisitorRequestService CreateService()
    {
        return new VisitorRequestService(
            _visitorRequestRepository.Object);
    }

    [Fact]
    public async Task CreateAsync_CreatesValidVisitorRequest()
    {
        // Arrange
        _visitorRequestRepository
            .Setup(repository => repository.CreateAsync(
                It.IsAny<VisitorRequest>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync((VisitorRequest request, CancellationToken _) =>
            {
                request.Id = "visitor-request-1";
                return request;
            });

        VisitorRequestService service = CreateService();

        var request = new CreateVisitorRequest
        {
            ResidentId = "resident-1",
            AccommodationId = "accommodation-1",
            VisitorName = "Thato Mokoena",
            VisitorIdentification = "0000000000000",
            ArrivalDateUtc = DateTime.UtcNow.AddDays(1),
            DepartureDateUtc = DateTime.UtcNow.AddDays(2),
            IsSleepover = true
        };

        // Act
        VisitorRequestResponse result =
            await service.CreateAsync(request);

        // Assert
        Assert.Equal("visitor-request-1", result.Id);
        Assert.Equal("resident-1", result.ResidentId);
        Assert.Equal("Thato Mokoena", result.VisitorName);
        Assert.True(result.IsSleepover);
        Assert.Equal(
            VisitorRequestStatuses.Pending,
            result.Status);

        _visitorRequestRepository.Verify(
            repository => repository.CreateAsync(
                It.Is<VisitorRequest>(visitor =>
                    visitor.ResidentId == "resident-1" &&
                    visitor.Status == VisitorRequestStatuses.Pending),
                It.IsAny<CancellationToken>()),
            Times.Once);
    }

    [Fact]
    public async Task CreateAsync_RejectsDepartureBeforeArrival()
    {
        // Arrange
        VisitorRequestService service = CreateService();

        DateTime arrival = DateTime.UtcNow.AddDays(2);

        var request = new CreateVisitorRequest
        {
            ResidentId = "resident-1",
            AccommodationId = "accommodation-1",
            VisitorName = "Thato Mokoena",
            VisitorIdentification = "0000000000000",
            ArrivalDateUtc = arrival,
            DepartureDateUtc = arrival.AddDays(-1),
            IsSleepover = false
        };

        // Act + Assert
        InvalidOperationException exception =
            await Assert.ThrowsAsync<InvalidOperationException>(
                () => service.CreateAsync(request));

        Assert.Equal(
            "Departure date cannot be before the arrival date.",
            exception.Message);

        _visitorRequestRepository.Verify(
            repository => repository.CreateAsync(
                It.IsAny<VisitorRequest>(),
                It.IsAny<CancellationToken>()),
            Times.Never);
    }

    [Fact]
    public async Task UpdateStatusAsync_ApprovesVisitorRequest()
    {
        // Arrange
        var visitorRequest = new VisitorRequest
        {
            Id = "visitor-request-1",
            ResidentId = "resident-1",
            AccommodationId = "accommodation-1",
            VisitorName = "Thato Mokoena",
            VisitorIdentification = "0000000000000",
            Status = VisitorRequestStatuses.Approved,
            CreatedAtUtc = DateTime.UtcNow,
            ReviewedAtUtc = DateTime.UtcNow
        };

        _visitorRequestRepository
            .Setup(repository => repository.UpdateStatusAsync(
                "visitor-request-1",
                VisitorRequestStatuses.Approved,
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(visitorRequest);

        VisitorRequestService service = CreateService();

        var request = new UpdateVisitorRequestStatusRequest
        {
            Status = "approved"
        };

        // Act
        VisitorRequestResponse? result =
            await service.UpdateStatusAsync(
                "visitor-request-1",
                request);

        // Assert
        Assert.NotNull(result);
        Assert.Equal(
            VisitorRequestStatuses.Approved,
            result.Status);

        _visitorRequestRepository.Verify(
            repository => repository.UpdateStatusAsync(
                "visitor-request-1",
                VisitorRequestStatuses.Approved,
                It.IsAny<CancellationToken>()),
            Times.Once);
    }

    [Fact]
    public async Task UpdateStatusAsync_RejectsInvalidStatus()
    {
        // Arrange
        VisitorRequestService service = CreateService();

        var request = new UpdateVisitorRequestStatusRequest
        {
            Status = "Maybe"
        };

        // Act + Assert
        InvalidOperationException exception =
            await Assert.ThrowsAsync<InvalidOperationException>(
                () => service.UpdateStatusAsync(
                    "visitor-request-1",
                    request));

        Assert.Equal(
            "Status must be Pending, Approved or Rejected.",
            exception.Message);

        _visitorRequestRepository.Verify(
            repository => repository.UpdateStatusAsync(
                It.IsAny<string>(),
                It.IsAny<string>(),
                It.IsAny<CancellationToken>()),
            Times.Never);
    }
}
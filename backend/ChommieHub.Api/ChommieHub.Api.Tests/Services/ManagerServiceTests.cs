using System;
using System.Collections.Generic;
using System.Text;
using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using Moq;
using Microsoft.Extensions.Logging.Abstractions;

using AccommodationApplication = ChommieHub.Api.Models.Application;

namespace ChommieHub.Api.Tests.Services;

/// <summary>
/// Verifies the residence-manager business rules without requiring a live Firestore connection
/// </summary>
public sealed class ManagerServiceTests
{
    private readonly Mock<IApplicationRepository> _applicationRepository = new();

    private readonly Mock<IResidentRepository> _residentRepository = new();

    private readonly Mock<IRoomRepository>  _roomRepository = new();

    private readonly Mock<IManagerWorkflowRepository>  _managerWorkflowRepository = new();

    private ManagerService CreateService()
    {
        return new ManagerService(
            _applicationRepository.Object,
            _residentRepository.Object,
            _roomRepository.Object,
            _managerWorkflowRepository.Object,
            null!,
            NullLogger<ManagerService>.Instance);
    }

    [Fact]
    public async Task ReviewApplicationAsync_ApprovesSubmittedApplication()
    {
        // Arrange
        var application = new AccommodationApplication
            {
                Id = "application-1",
                StudentId = "student-1",
                AccommodationId = "accommodation-1",
                RoomTypePreference = "Single Room",
                Status = ApplicationStatuses.Submitted,
                DateSubmittedUtc = DateTime.UtcNow
            };

        _applicationRepository.Setup(repository => repository.GetByIdAsync("application-1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(application);

        _applicationRepository.Setup(repository => repository.UpdateAsync(It.IsAny<AccommodationApplication>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(true);

        ManagerService service = CreateService();

        var request = new ReviewApplicationRequest
            {
                Decision = "Approved",
                ReviewNotes = "Application verified."
            };

        // Act
        ManagerApplicationDetailResponse? result = await service.ReviewApplicationAsync( "application-1",  request);

        // Assert
        Assert.NotNull(result);

        Assert.Equal(ApplicationStatuses.Approved, result.Status);

        Assert.Equal("Application verified.", result.ReviewNotes);

        Assert.NotNull(result.DateReviewedUtc);

        _applicationRepository.Verify( repository => repository.UpdateAsync(It.Is<AccommodationApplication>
            (updated =>updated.Status == ApplicationStatuses.Approved && updated.ReviewNotes == "Application verified."),
            It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task ReviewApplicationAsync_RejectsInvalidDecision()
    {
        // Arrange
        var application = new AccommodationApplication
            {
                Id = "application-1",
                AccommodationId = "accommodation-1",
                Status = ApplicationStatuses.Submitted
            };

        _applicationRepository.Setup(repository => repository.GetByIdAsync("application-1",It.IsAny<CancellationToken>()))
            .ReturnsAsync(application);

        ManagerService service = CreateService();

        var request = new ReviewApplicationRequest
            {
                Decision = "Maybe",
                ReviewNotes = string.Empty
            };

        // Act + Assert
        InvalidOperationException exception = await Assert.ThrowsAsync<InvalidOperationException>(
            () =>
                    service.ReviewApplicationAsync(
                        "application-1",
                        request));

        Assert.Equal("Decision must be either Approved or Declined.", exception.Message);
    }

    [Fact]
    public async Task GetDashboardAsync_CalculatesResidenceStatistics()
    {
        // Arrange
        IReadOnlyList<AccommodationApplication>applications = new List<AccommodationApplication>
        {
            new()
            {
                Status =ApplicationStatuses.Submitted
            },
            new()
            {
                Status = ApplicationStatuses.UnderReview
            },
            new()
            {
                Status =ApplicationStatuses.Approved
            },
            new()
            {
                Status = ApplicationStatuses.Declined
            }
        };

        IReadOnlyList<Resident> residents = new List<Resident>
        {
            new()
            {
                IsActive = true
            },
            new()
            {
                IsActive = true
            },
            new()
            {
             IsActive = false
            }
        };

        IReadOnlyList<Room> rooms = new List<Room>
        {
             new()
             {
                 Availability = true
             },
             new()
             {
                 Availability = true
             },
             new()
             {
                 Availability = false
             }
        };

        _applicationRepository.Setup(repository => repository.GetByAccommodationIdAsync("accommodation-1",It.IsAny<CancellationToken>()))
            .ReturnsAsync(applications);

        _residentRepository.Setup(repository => repository.GetByAccommodationIdAsync("accommodation-1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(residents);

        _roomRepository.Setup(repository => repository.GetByAccommodationIdAsync("accommodation-1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(rooms);

        ManagerService service = CreateService();

        // Act
        ManagerDashboardResponse result = await service.GetDashboardAsync("accommodation-1");

        // Assert
        Assert.Equal(4, result.TotalApplications);

        Assert.Equal(2, result.PendingApplications);

        Assert.Equal(1, result.ApprovedApplications);

        Assert.Equal(1, result.DeclinedApplications);

        Assert.Equal(2, result.ActiveResidents);

        Assert.Equal(3, result.TotalRooms);

        Assert.Equal(2, result.AvailableRooms);

        Assert.Equal(1, result.UnavailableRooms);
    }

    [Fact]
    public async Task AssignRoomAsync_UsesTransactionalWorkflowRepository()
    {
        // Arrange
        var resident = new Resident
        {
            Id = "application-1",
            StudentId = "student-1",
            AccommodationId = "accommodation-1",
            ApplicationId = "application-1",
            RoomId = "room-1",
            IsActive = true,
            CreatedAtUtc = DateTime.UtcNow
        };

        _managerWorkflowRepository.Setup(repository => repository.AssignRoomAsync("application-1", "room-1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(resident);

        ManagerService service = CreateService();

        var request =new AssignRoomRequest
        {
            RoomId = "room-1"
        };

        // Act
        ResidentResponse? result = await service.AssignRoomAsync("application-1", request);

        // Assert
        Assert.NotNull(result);

        Assert.Equal("student-1", result.StudentId);

        Assert.Equal("room-1", result.RoomId);

        Assert.True(result.IsActive);

        _managerWorkflowRepository.Verify(repository => repository.AssignRoomAsync("application-1", "room-1", It.IsAny<CancellationToken>()),
            Times.Once);
    }
}

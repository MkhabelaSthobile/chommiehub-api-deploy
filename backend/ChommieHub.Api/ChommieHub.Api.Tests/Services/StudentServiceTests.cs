using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using Moq;
using ApplicationModel = ChommieHub.Api.Models.Application;

namespace ChommieHub.Api.Tests.Services;

/// <summary>
/// Verifies student accommodation-application business rules without requiring a live Firestore connection.
/// </summary>
public sealed class StudentServiceTests
{
    private readonly Mock<IStudentRepository> _studentRepository = new();
    private readonly Mock<IApplicationRepository> _applicationRepository = new();
    private readonly Mock<IAccommodationRepository> _accommodationRepository = new();
    private readonly Mock<IRoomRepository> _roomRepository = new();

    private StudentService CreateService()
    {
        return new StudentService(_studentRepository.Object, _applicationRepository.Object, _accommodationRepository.Object, _roomRepository.Object);
    }

    [Fact]
    public async Task SubmitApplicationAsync_CreatesApplicationForSelectedAvailableRoom()
    {
        Student student = CreateStudent();
        Accommodation accommodation = CreateAccommodation();
        Room room = CreateRoom();

        _studentRepository.Setup(repository => repository.GetByIdAsync("student-1", It.IsAny<CancellationToken>())).ReturnsAsync(student);
        _accommodationRepository.Setup(repository => repository.GetByIdAsync("accommodation-1", It.IsAny<CancellationToken>())).ReturnsAsync(accommodation);
        _roomRepository.Setup(repository => repository.GetByIdAsync("room-1", It.IsAny<CancellationToken>())).ReturnsAsync(room);

        _applicationRepository.Setup(repository => repository.CreateAsync(It.IsAny<ApplicationModel>(), It.IsAny<CancellationToken>())).ReturnsAsync((ApplicationModel application, CancellationToken _) =>
        {
            application.Id = "application-1";
            return application;
        });

        SubmitApplicationRequest request = new()
        {
            AccommodationId = "accommodation-1",
            RoomId = "room-1",
            RentalPeriod = "January 2027 - November 2027",
            SupportingInfo = "First-year student."
        };

        StudentApplicationResponse result = await CreateService().SubmitApplicationAsync("student-1", request);

        Assert.Equal("application-1", result.Id);
        Assert.Equal("accommodation-1", result.AccommodationId);
        Assert.Equal("room-1", result.RequestedRoomId);
        Assert.Equal("Single Room", result.RoomTypePreference);
        Assert.Equal(ApplicationStatuses.Submitted, result.Status);

        _applicationRepository.Verify(repository => repository.CreateAsync(
            It.Is<ApplicationModel>(application => application.StudentId == "student-1" && application.RequestedRoomId == "room-1" && application.RoomTypePreference == "Single Room"),
            It.IsAny<CancellationToken>()),
            Times.Once);
    }

    [Fact]
    public async Task SubmitApplicationAsync_RejectsUnavailableRoom()
    {
        Student student = CreateStudent();
        Accommodation accommodation = CreateAccommodation();
        Room room = CreateRoom();
        room.Availability = false;

        _studentRepository.Setup(repository => repository.GetByIdAsync("student-1", It.IsAny<CancellationToken>())).ReturnsAsync(student);
        _accommodationRepository.Setup(repository => repository.GetByIdAsync("accommodation-1", It.IsAny<CancellationToken>())).ReturnsAsync(accommodation);
        _roomRepository.Setup(repository => repository.GetByIdAsync("room-1", It.IsAny<CancellationToken>())).ReturnsAsync(room);

        SubmitApplicationRequest request = new()
        {
            AccommodationId = "accommodation-1",
            RoomId = "room-1",
            RentalPeriod = "January 2027 - November 2027"
        };

        InvalidOperationException exception = await Assert.ThrowsAsync<InvalidOperationException>(() => CreateService().SubmitApplicationAsync("student-1", request));

        Assert.Equal("The selected room is no longer available.", exception.Message);

        _applicationRepository.Verify(repository => repository.CreateAsync(It.IsAny<ApplicationModel>(), It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task SubmitApplicationAsync_RejectsRoomFromDifferentAccommodation()
    {
        Student student = CreateStudent();
        Accommodation accommodation = CreateAccommodation();
        Room room = CreateRoom();
        room.AccommodationId = "another-accommodation";

        _studentRepository.Setup(repository => repository.GetByIdAsync("student-1", It.IsAny<CancellationToken>())).ReturnsAsync(student);
        _accommodationRepository.Setup(repository => repository.GetByIdAsync("accommodation-1", It.IsAny<CancellationToken>())).ReturnsAsync(accommodation);
        _roomRepository.Setup(repository => repository.GetByIdAsync("room-1", It.IsAny<CancellationToken>())).ReturnsAsync(room);

        SubmitApplicationRequest request = new()
        {
            AccommodationId = "accommodation-1",
            RoomId = "room-1",
            RentalPeriod = "January 2027 - November 2027"
        };

        InvalidOperationException exception = await Assert.ThrowsAsync<InvalidOperationException>(() => CreateService().SubmitApplicationAsync("student-1", request));

        Assert.Equal("The selected room does not belong to the selected accommodation.", exception.Message);

        _applicationRepository.Verify(repository => repository.CreateAsync(It.IsAny<ApplicationModel>(), It.IsAny<CancellationToken>()), Times.Never);
    }

    [Theory]
    [InlineData(ListingStatuses.Pending, false)]
    [InlineData(ListingStatuses.Rejected, false)]
    [InlineData(ListingStatuses.Draft, false)]
    [InlineData(ListingStatuses.Approved, true)]
    public async Task SubmitApplicationAsync_RejectsAccommodationHiddenFromStudents(string status, bool archived)
    {
        Student student = CreateStudent();
        Accommodation accommodation = CreateAccommodation();
        accommodation.ReviewStatus = status;
        accommodation.IsArchived = archived;

        _studentRepository.Setup(repository => repository.GetByIdAsync("student-1", It.IsAny<CancellationToken>())).ReturnsAsync(student);
        _accommodationRepository.Setup(repository => repository.GetByIdAsync("accommodation-1", It.IsAny<CancellationToken>())).ReturnsAsync(accommodation);

        SubmitApplicationRequest request = new()
        {
            AccommodationId = "accommodation-1",
            RoomId = "room-1",
            RentalPeriod = "January 2027 - November 2027",
            SupportingInfo = "First-year student."
        };

        InvalidOperationException exception = await Assert.ThrowsAsync<InvalidOperationException>(() => CreateService().SubmitApplicationAsync("student-1", request));

        Assert.Equal("The selected accommodation is not currently available.", exception.Message);

        _applicationRepository.Verify(repository => repository.CreateAsync(It.IsAny<ApplicationModel>(), It.IsAny<CancellationToken>()), Times.Never);
    }

    private static Student CreateStudent()
    {
        return new Student
        {
            Id = "student-1",
            FirebaseUid = "firebase-1",
            Name = "Lesego",
            Surname = "Student",
            Email = "student@example.com",
            UserType = "Applicant"
        };
    }

    private static Accommodation CreateAccommodation()
    {
        return new Accommodation
        {
            Id = "accommodation-1",
            Name = "Campus View",
            Address = "10 Main Road",
            IsActive = true
        };
    }

    private static Room CreateRoom()
    {
        return new Room
        {
            Id = "room-1",
            AccommodationId = "accommodation-1",
            RoomType = "Single Room",
            Price = 4500,
            Capacity = 1,
            Availability = true
        };
    }
}
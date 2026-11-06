using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using Moq;

namespace ChommieHub.Api.Tests.Services
{
    // Manager profiles may be created only through the protected administrator API.
    public sealed class ManagerAccountServiceTests
    {
        private readonly Mock<IManagerRepository> _managerRepository = new();
        private readonly Mock<IStudentRepository> _studentRepository = new();

        private ManagerAccountService CreateService() =>
            new(_managerRepository.Object, _studentRepository.Object);

        private static CreateManagerRequest CreateRequest() => new()
        {
            FirebaseUid = "uid-1",
            Name = " Thando ",
            Surname = "Mokoena",
            Email = "thando@example.com",
            PhoneNumber = "0821234567"
        };

        [Fact]
        public async Task CreateAsync_AppointmentStartsWithoutAccommodation()
        {
            _managerRepository.Setup(repository => repository.GetByIdAsync("uid-1", It.IsAny<CancellationToken>()))
                .ReturnsAsync((Manager?)null);
            _studentRepository.Setup(repository => repository.GetByIdAsync("uid-1", It.IsAny<CancellationToken>()))
                .ReturnsAsync((Student?)null);
            _managerRepository.Setup(repository => repository.CreateAsync(It.IsAny<Manager>(), It.IsAny<CancellationToken>()))
                .ReturnsAsync((Manager manager, CancellationToken _) =>
                {
                    manager.Id = manager.FirebaseUid;
                    return manager;
                });

            ManagerResponse result = await CreateService().CreateAsync(CreateRequest());

            Assert.Equal("uid-1", result.Id);
            Assert.Equal("Thando", result.Name);
            Assert.Equal(string.Empty, result.AccommodationId);
        }

        [Fact]
        public async Task CreateAsync_RejectsExistingManager()
        {
            _managerRepository.Setup(repository => repository.GetByIdAsync("uid-1", It.IsAny<CancellationToken>()))
                .ReturnsAsync(new Manager { Id = "uid-1", FirebaseUid = "uid-1" });

            await Assert.ThrowsAsync<InvalidOperationException>(
                () => CreateService().CreateAsync(CreateRequest()));

            _managerRepository.Verify(repository =>
                repository.CreateAsync(It.IsAny<Manager>(), It.IsAny<CancellationToken>()), Times.Never);
        }

        [Fact]
        public async Task CreateAsync_RejectsExistingStudentAccount()
        {
            _managerRepository.Setup(repository => repository.GetByIdAsync("uid-1", It.IsAny<CancellationToken>()))
                .ReturnsAsync((Manager?)null);
            _studentRepository.Setup(repository => repository.GetByIdAsync("uid-1", It.IsAny<CancellationToken>()))
                .ReturnsAsync(new Student { Id = "uid-1" });

            await Assert.ThrowsAsync<InvalidOperationException>(
                () => CreateService().CreateAsync(CreateRequest()));

            _managerRepository.Verify(repository =>
                repository.CreateAsync(It.IsAny<Manager>(), It.IsAny<CancellationToken>()), Times.Never);
        }

        [Fact]
        public async Task CreateAsync_RejectsMissingFirebaseUid()
        {
            var request = CreateRequest();
            request.FirebaseUid = " ";

            await Assert.ThrowsAsync<ArgumentException>(
                () => CreateService().CreateAsync(request));
        }
    }
}

using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using Google.Cloud.Firestore.V1;
using Moq;
using System;
using System.Collections.Generic;
using System.Data;
using System.Text;

namespace ChommieHub.Api.Tests.Services
{
    // Verifies manager self - registration rules without a live Firestore connection.
    public sealed class ManagerAccountServiceTests
    {
        private readonly Mock<IManagerRepository> _managerRepository = new();
        private readonly Mock<IStudentRepository> _studentRepository = new();

        private ManagerAccountService CreateService()
        {
            return new ManagerAccountService(_managerRepository.Object, _studentRepository.Object);
        }

        private static RegisterManagerRequest CreateRequest()
        {
            return new RegisterManagerRequest
            {
                Name = " Thando ",
                Surname = "Mokoena",
                Email = "thando@example.com",
                PhoneNumber = "0821234567"
            };
        }

        [Fact]
        public async Task RegisterAsync_CreatesManagerWithoutAccommodation()
        {
            _managerRepository.Setup(repository => repository.GetByIdAsync("uid-1", It.IsAny<CancellationToken>())).ReturnsAsync((Manager?)null);
            _studentRepository.Setup(repository => repository.GetByIdAsync("uid-1", It.IsAny<CancellationToken>())).ReturnsAsync((Student?)null);
            _managerRepository.Setup(repository => repository.CreateAsync(It.IsAny<Manager>(), It.IsAny<CancellationToken>()))
                .ReturnsAsync((Manager manager, CancellationToken _) =>
                {
                    manager.Id = manager.FirebaseUid;
                    return manager;
                });

            ManagerResponse result = await CreateService().RegisterAsync("uid-1", CreateRequest());

            Assert.Equal("uid-1", result.Id);
            Assert.Equal("Thando", result.Name);
            Assert.Equal(string.Empty, result.AccommodationId);
        }

        [Fact]
        public async Task RegisterAsync_RejectsAnExistingManager()
        {
            _managerRepository.Setup(repository => repository.GetByIdAsync("uid-1", It.IsAny<CancellationToken>())).ReturnsAsync(new Manager { Id = "uid-1", FirebaseUid = "uid-1" });

            await Assert.ThrowsAsync<InvalidOperationException>(() => CreateService().RegisterAsync("uid-1", CreateRequest()));

            _managerRepository.Verify(repository => repository.CreateAsync(It.IsAny<Manager>(), It.IsAny<CancellationToken>()), Times.Never);
        }

        [Fact]
        public async Task RegisterAsync_RejectsAnExistingStudentAccount()
        {
            _managerRepository.Setup(repository => repository.GetByIdAsync("uid-1", It.IsAny<CancellationToken>())).ReturnsAsync((Manager?)null);
            _studentRepository.Setup(repository => repository.GetByIdAsync("uid-1", It.IsAny<CancellationToken>())).ReturnsAsync(new Student { Id = "uid-1" });

            await Assert.ThrowsAsync<InvalidOperationException>(() => CreateService().RegisterAsync("uid-1", CreateRequest()));

            _managerRepository.Verify(repository => repository.CreateAsync(It.IsAny<Manager>(), It.IsAny<CancellationToken>()), Times.Never);
        }
    }
}

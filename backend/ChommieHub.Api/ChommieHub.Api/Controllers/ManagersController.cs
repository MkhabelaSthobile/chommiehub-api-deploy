using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{

    [ApiController]
    [Route("api/managers")]
    public sealed class ManagersController : ControllerBase
    {
        private readonly IManagerAccountService _managerAccountService;
        private readonly ILogger<ManagersController> _logger;

        public ManagersController(IManagerAccountService managerAccountService, ILogger<ManagersController> logger)
        {
            _managerAccountService = managerAccountService;
            _logger = logger;
        }


        [HttpGet("{id}")]
        public async Task<ActionResult<ManagerResponse>> GetById(string id, CancellationToken cancellationToken)
        {
            ManagerResponse? manager = await _managerAccountService.GetByIdAsync(id, cancellationToken);

            if (manager is null)
            {
                return NotFound(new { message = "Manager profile was not found." });
            }

            return Ok(manager);
        }

        [HttpGet("by-firebase-uid/{firebaseUid}")]
        public Task<ActionResult<ManagerResponse>> GetByFirebaseUid(string firebaseUid, CancellationToken cancellationToken)
            => GetById(firebaseUid, cancellationToken);

        [HttpPost]
        public async Task<ActionResult<ManagerResponse>> Create(CreateManagerRequest request, CancellationToken cancellationToken)
        {
            ManagerResponse manager = await _managerAccountService.CreateAsync(request, cancellationToken);

            _logger.LogInformation("Manager profile {ManagerId} was created.", manager.Id);

            return CreatedAtAction(nameof(GetById), new { id = manager.Id }, manager);
        }
    }
}

using ChommieHub.Api.Configuration;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Repositories.Interfaces;
using ChommieHub.Api.Services;
using ChommieHub.Api.Services.Interfaces;
using Google.Cloud.Firestore;
using Microsoft.Extensions.Options;

var builder = WebApplication.CreateBuilder(args);

string? renderPort = Environment.GetEnvironmentVariable("PORT");

if (!string.IsNullOrWhiteSpace(renderPort))
{
    builder.WebHost.UseUrls($"http://0.0.0.0:{renderPort}");
}

// ---------------------------------------------------------
// ASP.NET Core framework services
// ---------------------------------------------------------

builder.Services.AddControllers();
builder.Services.AddOpenApi();

// ---------------------------------------------------------
// Firestore configuration
// ---------------------------------------------------------
//
// Local development:
// GOOGLE_APPLICATION_CREDENTIALS points to the Firebase service-account JSON.
//
// Render:
// The shared deployed API uses the service credentials configured in Render.
//
// The credential itself must never be committed to GitHub.
// ---------------------------------------------------------

builder.Services.Configure<FirestoreOptions>(builder.Configuration.GetSection(FirestoreOptions.SectionName));

builder.Services.AddSingleton(sp =>
{
    FirestoreOptions options = sp.GetRequiredService<IOptions<FirestoreOptions>>().Value;

    if (string.IsNullOrWhiteSpace(options.ProjectId))
    {
        throw new InvalidOperationException("Firestore:ProjectId has not been configured.");
    }

    ILogger logger = sp.GetRequiredService<ILoggerFactory>().CreateLogger("FirestoreConfiguration");

    logger.LogInformation("Connecting ChommieHub API to Firestore project {ProjectId}.", options.ProjectId);

    return FirestoreDb.Create(options.ProjectId);
});

// ---------------------------------------------------------
// Firestore repositories
// ---------------------------------------------------------

builder.Services.AddScoped<IAccommodationRepository, FirestoreAccommodationRepository>();
builder.Services.AddScoped<IRoomRepository, FirestoreRoomRepository>();
builder.Services.AddScoped<IApplicationRepository, FirestoreApplicationRepository>();
builder.Services.AddScoped<IResidentRepository, FirestoreResidentRepository>();
builder.Services.AddScoped<IStudentRepository, FirestoreStudentRepository>();
builder.Services.AddScoped<IManagerRepository, FirestoreManagerRepository>();
builder.Services.AddScoped<IManagerWorkflowRepository, FirestoreManagerWorkflowRepository>();
builder.Services.AddScoped<IMaintenanceRepository, FirestoreMaintenanceRepository>();
builder.Services.AddScoped<IVisitorRequestRepository, FirestoreVisitorRequestRepository>();
builder.Services.AddScoped<IShortlistRepository, FirestoreShortlistRepository>();
builder.Services.AddScoped<IInspectionRepository, FirestoreInspectionRepository>();

// ---------------------------------------------------------
// Business services
// ---------------------------------------------------------

builder.Services.AddScoped<IAccommodationService, AccommodationService>();
builder.Services.AddScoped<IRoomService, RoomService>();
builder.Services.AddScoped<IManagerService, ManagerService>();
builder.Services.AddScoped<IMaintenanceService, MaintenanceService>();
builder.Services.AddScoped<IVisitorRequestService, VisitorRequestService>();
builder.Services.AddScoped<IShortlistService, ShortlistService>();
builder.Services.AddScoped<IStudentService, StudentService>();
builder.Services.AddScoped<IManagerAccountService, ManagerAccountService>();
builder.Services.AddScoped<IInspectionService, InspectionService>();

// Weather-aware residence operations from the previous feature set.
builder.Services.AddHttpClient<IWeatherService, WeatherService>();

var app = builder.Build();

// Render terminates public HTTPS before forwarding the request to the container.
// The ASP.NET Core application therefore listens internally over HTTP.
if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.MapControllers();

app.Run();
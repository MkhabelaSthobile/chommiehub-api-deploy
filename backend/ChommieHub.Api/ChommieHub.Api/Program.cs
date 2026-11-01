
using ChommieHub.Api.Configuration;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Repositories.Interfaces;
using ChommieHub.Api.Services;
using ChommieHub.Api.Services.Interfaces;
using FirebaseAdmin;
using Google.Apis.Auth.OAuth2;
using Google.Cloud.Firestore;
using Microsoft.Extensions.Options;

var builder = WebApplication.CreateBuilder(args);

// ---------------------------------------------------------
// Render hosting configuration
// ---------------------------------------------------------

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
// GOOGLE_APPLICATION_CREDENTIALS points to the Firebase
// service-account JSON file.
//
// Render:
// The deployed API must have valid Google credentials
// configured securely in its environment.
//
// Never commit service-account credentials to GitHub.
// ---------------------------------------------------------

builder.Services.Configure<FirestoreOptions>(
    builder.Configuration.GetSection(FirestoreOptions.SectionName)
);

builder.Services.AddSingleton<FirestoreDb>(sp =>
{
    FirestoreOptions options = sp
        .GetRequiredService<IOptions<FirestoreOptions>>()
        .Value;

    if (string.IsNullOrWhiteSpace(options.ProjectId))
    {
        throw new InvalidOperationException(
            "Firestore:ProjectId has not been configured."
        );
    }

    ILogger logger = sp
        .GetRequiredService<ILoggerFactory>()
        .CreateLogger("FirestoreConfiguration");

    logger.LogInformation(
        "Connecting ChommieHub API to Firestore project {ProjectId}.",
        options.ProjectId
    );

    return FirestoreDb.Create(options.ProjectId);
});

// ---------------------------------------------------------
// Firebase Admin SDK configuration
// ---------------------------------------------------------
//
// Firebase Admin SDK is used for:
// 1. Verifying Firebase Authentication ID tokens.
// 2. Sending Firebase Cloud Messaging notifications.
//
// Uses Application Default Credentials (ADC).
// The credentials must belong to the correct Firebase project.
// ---------------------------------------------------------

builder.Services.AddSingleton<FirebaseApp>(sp =>
{
    IConfiguration configuration =
        sp.GetRequiredService<IConfiguration>();

    ILogger logger = sp
        .GetRequiredService<ILoggerFactory>()
        .CreateLogger("FirebaseAdminConfiguration");

    string? projectId = configuration["Firestore:ProjectId"];

    if (string.IsNullOrWhiteSpace(projectId))
    {
        throw new InvalidOperationException(
            "Firestore:ProjectId must be configured for Firebase Admin."
        );
    }

    GoogleCredential credential =
        GoogleCredential.GetApplicationDefault();

    logger.LogInformation(
        "Initializing Firebase Admin SDK for project {ProjectId}.",
        projectId
    );

    return FirebaseApp.Create(new AppOptions
    {
        Credential = credential,
        ProjectId = projectId
    });
});

// ---------------------------------------------------------
// Firestore repositories
// ---------------------------------------------------------

builder.Services.AddScoped<
    IAccommodationRepository,
    FirestoreAccommodationRepository>();

builder.Services.AddScoped<
    IRoomRepository,
    FirestoreRoomRepository>();

builder.Services.AddScoped<
    IApplicationRepository,
    FirestoreApplicationRepository>();

builder.Services.AddScoped<
    IResidentRepository,
    FirestoreResidentRepository>();

builder.Services.AddScoped<
    IStudentRepository,
    FirestoreStudentRepository>();

builder.Services.AddScoped<
    IManagerRepository,
    FirestoreManagerRepository>();

builder.Services.AddScoped<
    IManagerWorkflowRepository,
    FirestoreManagerWorkflowRepository>();

builder.Services.AddScoped<
    IMaintenanceRepository,
    FirestoreMaintenanceRepository>();

builder.Services.AddScoped<
    IVisitorRequestRepository,
    FirestoreVisitorRequestRepository>();

builder.Services.AddScoped<
    IShortlistRepository,
    FirestoreShortlistRepository>();

builder.Services.AddScoped<
    IInspectionRepository,
    FirestoreInspectionRepository>();

builder.Services.AddScoped<
    FirestoreFcmDeviceRepository>();

builder.Services.AddScoped<
    FirebaseNotificationService>();

// ---------------------------------------------------------
// Business services
// ---------------------------------------------------------

builder.Services.AddScoped<
    IAccommodationService,
    AccommodationService>();

builder.Services.AddScoped<
    IRoomService,
    RoomService>();

builder.Services.AddScoped<
    IManagerService,
    ManagerService>();

builder.Services.AddScoped<
    IMaintenanceService,
    MaintenanceService>();

builder.Services.AddScoped<
    IVisitorRequestService,
    VisitorRequestService>();

builder.Services.AddScoped<
    IShortlistService,
    ShortlistService>();

builder.Services.AddScoped<
    IStudentService,
    StudentService>();

builder.Services.AddScoped<
    IManagerAccountService,
    ManagerAccountService>();

builder.Services.AddScoped<
    IManagerListingService,
    ManagerListingService>();

builder.Services.AddScoped<
    IInspectionService,
    InspectionService>();

// Weather-aware residence operations.
builder.Services.AddHttpClient<
    IWeatherService,
    WeatherService>();

// ---------------------------------------------------------
// Firebase authentication services
// ---------------------------------------------------------

builder.Services.AddScoped<FirebaseTokenVerificationService>();

builder.Services.AddScoped<
    IAdminAccessVerifier,
    FirebaseAdminAccessVerifier>();

builder.Services.AddScoped<
    IFirebaseAccountAdmin,
    FirebaseAccountAdminService>();

builder.Services.AddScoped<
    IAuditRepository,
    FirestoreAuditRepository>();

// ---------------------------------------------------------
// Build the ASP.NET Core application
// ---------------------------------------------------------

var app = builder.Build();

// Render terminates public HTTPS before forwarding requests
// to the container. The API listens internally over HTTP.

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

// ---------------------------------------------------------
// API endpoints
// ---------------------------------------------------------

app.MapControllers();

app.Run();

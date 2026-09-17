using ChommieHub.Api.Configuration;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using Google.Cloud.Firestore;
using Microsoft.Extensions.Options;

var builder = WebApplication.CreateBuilder(args);

// ---------------------------------------------------------
// ChommieHub API service registration
// ---------------------------------------------------------

builder.Services.AddControllers();
builder.Services.AddOpenApi();

// Bind the Firestore section from appsettings.json.
builder.Services.Configure<FirestoreOptions>(builder.Configuration.GetSection(FirestoreOptions.SectionName));

//Register one Firestore database connection for the application
builder.Services.AddSingleton(sp =>
{
    var options = sp
        .GetRequiredService<IOptions<FirestoreOptions>>()
        .Value;

    if (string.IsNullOrWhiteSpace(options.ProjectId))
    {
        throw new InvalidOperationException("Firestore:ProjectId has not been configured.");
    }

    return FirestoreDb.Create(options.ProjectId);
});

// Firestore persistence repositories
builder.Services.AddScoped<IAccommodationRepository, FirestoreAccommodationRepository>();

builder.Services.AddScoped<IRoomRepository, FirestoreRoomRepository>();

builder.Services.AddScoped<IStudentRepository, FirestoreStudentRepository>();

builder.Services.AddScoped<IApplicationRepository, FirestoreApplicationRepository>();

builder.Services.AddScoped<
    IResidentRepository,
    FirestoreResidentRepository>();

//Application business services
builder.Services.AddScoped<IAccommodationService, AccommodationService>();

builder.Services.AddScoped<IRoomService, RoomService>();

var app = builder.Build();

// ---------------------------------------------------------
// HTTP request pipeline
// ---------------------------------------------------------

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseHttpsRedirection();

app.MapControllers();

app.Run();
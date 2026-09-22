# ============================================================
# Build stage — compiles the ASP.NET Core project
# ============================================================
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src

# Copy the project file and restore dependencies first.
# Doing this before copying the rest of the source lets Docker
# cache the restore layer, so rebuilds are fast when only
# application code changes.
COPY ChommieHub.Api.csproj ./
RUN dotnet restore ChommieHub.Api.csproj

# Copy the rest of the source and publish a Release build.
COPY . ./
RUN dotnet publish ChommieHub.Api.csproj \
    -c Release \
    -o /app/publish \
    /p:UseAppHost=false

# ============================================================
# Runtime stage — smaller image with just the published output
# ============================================================
FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS runtime
WORKDIR /app

# Copy the published output from the build stage.
COPY --from=build /app/publish ./

# Render expects the app to listen on the port given by the
# PORT environment variable. If it's not set, fall back to 8080.
ENV ASPNETCORE_URLS=http://+:8080
EXPOSE 8080

ENTRYPOINT ["dotnet", "ChommieHub.Api.dll"]
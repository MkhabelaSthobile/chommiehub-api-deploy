# ============================================================
# Build stage
# ============================================================
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build

WORKDIR /src

# Copy the project file first so Docker can cache package restore.
COPY ChommieHub.Api.csproj ./

RUN dotnet restore ChommieHub.Api.csproj

# Copy all remaining API source files.
COPY . ./

# Publish an optimised Release build for the runtime container.
RUN dotnet publish ChommieHub.Api.csproj -c Release -o /app/publish /p:UseAppHost=false

# ============================================================
# Runtime stage
# ============================================================
FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS runtime

WORKDIR /app

# Copy the published API from the build container.
COPY --from=build /app/publish ./

# Provides a safe fallback when PORT is not supplied.
# Render supplies PORT automatically and Program.cs uses that value.
ENV ASPNETCORE_URLS=http://0.0.0.0:10000

EXPOSE 10000

ENTRYPOINT ["dotnet", "ChommieHub.Api.dll"]
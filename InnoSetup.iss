[Setup]
AppId={{863670cf-a7a6-4314-9479-c99e1c6fce06}
AppName=ThunderFocus
AppVersion=2.7.0
AppVerName=ThunderFocus v2.7.0
AppPublisher=marcocipriani01
AppPublisherURL=https://marcocipriani01.github.io/
AppSupportURL=https://marcocipriani01.github.io/
AppUpdatesURL=https://marcocipriani01.github.io/
DefaultDirName={autopf}\ThunderFocus
DisableDirPage=yes
DefaultGroupName=ThunderFocus
DisableProgramGroupPage=yes
DisableReadyPage=yes
LicenseFile=.\INSTALLATION_NOTICE.txt
Compression=lzma
SolidCompression=yes
WizardStyle=modern
OutputBaseFilename=ThunderFocus_Win_64bit
OutputDir=Installers
WizardImageFile=".\WizardImage.bmp"
SetupIconFile=".\ASCOM.ThunderFocus\Resources\icon.ico"
UninstallDisplayIcon=".\ASCOM.ThunderFocus\Resources\icon.ico"
ChangesAssociations = yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "italian"; MessagesFile: "compiler:Languages\Italian.isl"

[Dirs]
Name: "{app}"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: ".\JRE-bundle\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: ".\ASCOM.ThunderFocus\ASCOM.ThunderFocus.Focuser\bin\Release\*"; DestDir: "{commoncf}\ASCOM\Focuser\ASCOM.ThunderFocus.Focuser\"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: ".\ASCOM.ThunderFocus\ASCOM.ThunderFocus.CoverCalibrator\bin\Release\*"; DestDir: "{commoncf}\ASCOM\CoverCalibrator\ASCOM.ThunderFocus.CoverCalibrator\"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: ".\ASCOM.ThunderFocus\ASCOM.ThunderFocus.Switch\bin\Release\*"; DestDir: "{commoncf}\ASCOM\Switch\ASCOM.ThunderFocus.Switch\"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\ThunderFocus"; Filename: "{app}\bin\javaw.exe"; IconFilename: "{app}\icon.ico"; Parameters: "-jar ""{app}\ThunderFocus.jar"""
Name: "{group}\ThunderFocus debug"; Filename: "{app}\bin\java.exe"; IconFilename: "{app}\debug.ico"; Parameters: "-jar ""{app}\ThunderFocus.jar"""
Name: "{group}\{cm:UninstallProgram,ThunderFocus}"; IconFilename: "{app}\uninstall.ico"; Filename: "{uninstallexe}"
Name: "{autodesktop}\ThunderFocus"; Filename: "{app}\bin\javaw.exe"; Tasks: desktopicon; IconFilename: "{app}\icon.ico"; Parameters: "-jar ""{app}\ThunderFocus.jar"""

[Run]
Filename: "{dotnet4032}\RegAsm.exe"; Parameters: "/codebase ""{commoncf}\ASCOM\Focuser\ASCOM.ThunderFocus.Focuser\ASCOM.ThunderFocus.Focuser.dll"""; Flags: runhidden 32bit
Filename: "{dotnet4064}\RegAsm.exe"; Parameters: "/codebase ""{commoncf}\ASCOM\Focuser\ASCOM.ThunderFocus.Focuser\ASCOM.ThunderFocus.Focuser.dll"""; Flags: runhidden 64bit; Check: IsWin64
Filename: "{dotnet4032}\RegAsm.exe"; Parameters: "/codebase ""{commoncf}\ASCOM\CoverCalibrator\ASCOM.ThunderFocus.CoverCalibrator\ASCOM.ThunderFocus.CoverCalibrator.dll"""; Flags: runhidden 32bit
Filename: "{dotnet4064}\RegAsm.exe"; Parameters: "/codebase ""{commoncf}\ASCOM\CoverCalibrator\ASCOM.ThunderFocus.CoverCalibrator\ASCOM.ThunderFocus.CoverCalibrator.dll"""; Flags: runhidden 64bit; Check: IsWin64
Filename: "{dotnet4032}\RegAsm.exe"; Parameters: "/codebase ""{commoncf}\ASCOM\Switch\ASCOM.ThunderFocus.Switch\ASCOM.ThunderFocus.Switch.dll"""; Flags: runhidden 32bit
Filename: "{dotnet4064}\RegAsm.exe"; Parameters: "/codebase ""{commoncf}\ASCOM\Switch\ASCOM.ThunderFocus.Switch\ASCOM.ThunderFocus.Switch.dll"""; Flags: runhidden 64bit; Check: IsWin64
Filename: "{app}\bin\javaw.exe"; Parameters: "-jar ""{app}\ThunderFocus.jar"""; Description: "{cm:LaunchProgram,ThunderFocus}"; Flags: nowait postinstall skipifsilent

[UninstallRun]
Filename: "{dotnet4032}\regasm.exe"; Parameters: "-u ""{commoncf}\ASCOM\Focuser\ASCOM.ThunderFocus.Focuser.dll"""; Flags: runhidden 32bit; RunOnceId: "RemoveDDL1"
Filename: "{dotnet4064}\regasm.exe"; Parameters: "/codebase ""{commoncf}\ASCOM\Focuser\ASCOM.ThunderFocus.Focuser.dll"""; Flags: runhidden 64bit; Check: IsWin64; RunOnceId: "RemoveDDL2"
Filename: "{dotnet4064}\regasm.exe"; Parameters: "-u ""{commoncf}\ASCOM\Focuser\ASCOM.ThunderFocus.Focuser.dll"""; Flags: runhidden 64bit; Check: IsWin64; RunOnceId: "RemoveDDL3"
Filename: "{dotnet4032}\regasm.exe"; Parameters: "-u ""{commoncf}\ASCOM\CoverCalibrator\ASCOM.ThunderFocus.CoverCalibrator.dll"""; Flags: runhidden 32bit; RunOnceId: "RemoveDDL4"
Filename: "{dotnet4064}\regasm.exe"; Parameters: "/codebase ""{commoncf}\ASCOM\CoverCalibrator\ASCOM.ThunderFocus.CoverCalibrator.dll"""; Flags: runhidden 64bit; Check: IsWin64; RunOnceId: "RemoveDDL5"
Filename: "{dotnet4064}\regasm.exe"; Parameters: "-u ""{commoncf}\ASCOM\CoverCalibrator\ASCOM.ThunderFocus.CoverCalibrator.dll"""; Flags: runhidden 64bit; Check: IsWin64; RunOnceId: "RemoveDDL6"
Filename: "{dotnet4032}\regasm.exe"; Parameters: "-u ""{commoncf}\ASCOM\Switch\ASCOM.ThunderFocus.Switch.dll"""; Flags: runhidden 32bit; RunOnceId: "RemoveDDL7"
Filename: "{dotnet4064}\regasm.exe"; Parameters: "/codebase ""{commoncf}\ASCOM\Switch\ASCOM.ThunderFocus.Switch.dll"""; Flags: runhidden 64bit; Check: IsWin64; RunOnceId: "RemoveDDL8"
Filename: "{dotnet4064}\regasm.exe"; Parameters: "-u ""{commoncf}\ASCOM\Switch\ASCOM.ThunderFocus.Switch.dll"""; Flags: runhidden 64bit; Check: IsWin64; RunOnceId: "RemoveDDL9"

[Registry]
Root: HKCR; Subkey: ".thunder"; ValueData: "ThunderFocus"; Flags: uninsdeletevalue; ValueType: string; ValueName: ""
Root: HKCR; Subkey: "ThunderFocus"; ValueData: "ThunderFocus config file"; Flags: uninsdeletekey; ValueType: string; ValueName: ""
Root: HKCR; Subkey: "ThunderFocus\DefaultIcon"; ValueData: "{app}\icon.ico"; ValueType: string; ValueName: ""
Root: HKCR; Subkey: "ThunderFocus\shell\open\command"; ValueData: """{app}\bin\javaw.exe"" ""-jar"" ""{app}\ThunderFocus.jar"" ""%1"""; ValueType: string; ValueName: ""

[Code]
const
   REQUIRED_PLATFORM_VERSION = 6.5;    // Set this to the minimum required ASCOM Platform version for this application

//
// Function to return the ASCOM Platform's version number as a double.
//
function PlatformVersion(): Double;
var
   PlatVerString : String;
begin
   Result := 0.0;  // Initialise the return value in case we can't read the registry
   try
      if RegQueryStringValue(HKEY_LOCAL_MACHINE_32, 'Software\ASCOM','PlatformVersion', PlatVerString) then 
      begin // Successfully read the value from the registry
         Result := StrToFloat(PlatVerString); // Create a double from the X.Y Platform version string
      end;
   except                                                                   
      ShowExceptionMessage;
      Result:= -1.0; // Indicate in the return value that an exception was generated
   end;
end;

//
// Before the installer UI appears, verify that the required ASCOM Platform version is installed.
//
function InitializeSetup(): Boolean;
var
   PlatformVersionNumber : double;
 begin
   Result := FALSE;  // Assume failure
   PlatformVersionNumber := PlatformVersion(); // Get the installed Platform version as a double
   If PlatformVersionNumber >= REQUIRED_PLATFORM_VERSION then	// Check whether we have the minimum required Platform or newer
      Result := TRUE
   else
      if PlatformVersionNumber = 0.0 then
         MsgBox('No ASCOM Platform is installed. Please install Platform ' + Format('%3.1f', [REQUIRED_PLATFORM_VERSION]) + ' or later from https://www.ascom-standards.org', mbCriticalError, MB_OK)
      else 
         MsgBox('ASCOM Platform ' + Format('%3.1f', [REQUIRED_PLATFORM_VERSION]) + ' or later is required, but Platform '+ Format('%3.1f', [PlatformVersionNumber]) + ' is installed. Please install the latest Platform before continuing; you will find it at https://www.ascom-standards.org', mbCriticalError, MB_OK);
end;

// Code to enable the installer to uninstall previous versions of itself when a new version is installed
procedure CurStepChanged(CurStep: TSetupStep);
var
  ResultCode: Integer;
  UninstallExe: String;
  UninstallRegistry: String;
begin
  if (CurStep = ssInstall) then // Install step has started
	begin
      // Create the correct registry location name, which is based on the AppId
      UninstallRegistry := ExpandConstant('Software\Microsoft\Windows\CurrentVersion\Uninstall\{#SetupSetting("AppId")}' + '_is1');
      // Check whether an extry exists
      if RegQueryStringValue(HKLM, UninstallRegistry, 'UninstallString', UninstallExe) then
        begin // Entry exists and previous version is installed so run its uninstaller quietly after informing the user
          Exec(RemoveQuotes(UninstallExe), ' /SILENT', '', SW_SHOWNORMAL, ewWaitUntilTerminated, ResultCode);
          sleep(1000);    //Give enough time for the install screen to be repainted before continuing
        end
  end;
end;
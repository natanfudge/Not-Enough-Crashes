# Configuring Not Enough Crashes

Use the mod configuration screen, or edit `config/notenoughcrashes.json` in your Minecraft instance while the game is closed. Your launcher can open the instance folder.

| Setting | Default | Effect |
| --- | --- | --- |
| `disableReturnToMainMenu` | `false` | Disable returning to the title screen after a crash. |
| `catchInitializationCrashes` | `true` | Show the startup error screen on supported Fabric configurations. |
| `catchGameloop` | `true` | Attempt recovery from gameplay crashes. Automatically disabled with Iris. |
| `crashLimit` | `20` | Maximum recovery attempts in a session. |
| `debugModIdentification` | `false` | Log additional suspected-mod diagnostics. |
| `modpackIssueTracker` | `""` | HTTP(S) issue-tracker URL for the modpack. Empty hides the button. |

## Modpack reports

Set `modpackIssueTracker` to your pack's issue list or report form, for example `https://github.com/your-team/your-pack/issues`.

**Report to modpack** copies the shared paste link (if uploaded) or the crash report to the clipboard, then asks before opening the tracker. Players can check existing issues and describe what happened before submitting. NEC does not post issues or require a GitHub token.

## Local reports and sharing

Reports are saved under `crash-reports`. **Show crash log** and the green report filename open that local file. **Upload to pastes.dev** uploads only when clicked; it copies the resulting link and offers to open it. Failed uploads can be retried.

**Export reproduction ZIP** creates a local `crash-reports/instance-*.zip` containing the report, mod IDs and versions, and the player's reproduction notes. Configs and the crashed singleplayer world are separate opt-ins, both off by default. No world is offered for multiplayer or startup crashes. Mod binaries, account files, and server lists are not collected. Symbolic links and world session locks are skipped.

Reports, configs and worlds can contain private information. Review the archive before attaching it to an issue. No archive is uploaded automatically.

Legacy `forceCrashScreen`, `deobfuscateStackTrace`, and `crashlogUpload` settings are obsolete.

tell application "Terminal"
	quit
	delay 1
end tell

set testcmd_jar to null
set testcmd_jvm_flags to "-ea"
set testcmd_nickname to "$"
set testcmd_address to "localhost"
set testcmd_port to "9999"

set self to POSIX path of (path to me as string)
set ownParent to text 1 thru -((length of (last word of self)) + 1) of self
set pairs to paragraphs of (read ownParent & "test.ini")

set AppleScript's text item delimiters to "="
repeat with pair in pairs
	if pair as string is not equal to "" then
		set pieces to text items of pair
		set kee to item 1 of pieces
		set value to item 2 of pieces
		if kee is equal to "testcmd_jar" then
			set testcmd_jar to value
		else if kee is equal to "testcmd_jvm_flags" then
			set testcmd_jvm_flags to value
		else if kee is equal to "testcmd_nickname" then
			set testcmd_nickname to value
		else if kee is equal to "testcmd_address" then
			set testcmd_address to value
		else if kee is equal to "testcmd_port" then
			set testcmd_port to value
		end if
	end if
end repeat

if testcmd_jar is equal to null then
	log "testcmd_jar not defined. Aborting."
	return
end if

tell application "Terminal"
	activate
	set zoomed of window 1 to true
	
	set cmdline to "cd " & ownParent & "&& java " & testcmd_jvm_flags & " -jar " & testcmd_jar
	
	do script (cmdline & " server " & testcmd_port) in window 1
	
	delay 1
	tell application "System Events" to keystroke "t" using {command down}
	
	delay 1
	do script (cmdline & " client " & testcmd_address & ":" & testcmd_port & " " & testcmd_nickname) in window 1
end tell

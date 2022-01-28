# TeslaInventoryTracker
Automated tool to send sms if there's stock in inventory

1. Please setup redis locally. One-click docker command: `docker run --name recorder-redis -p 6379:6379 -d redis:alpine`
Note: This program does not take remote redis into consideration thus no implementation on connecting redis via URL.
2. Chang config file to tailor the automation

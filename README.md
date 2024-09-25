# CheckYourChest

**CheckYourChest** is a Minecraft utility mod designed for both servers and single-player worlds, perfect for players managing farms or automated item systems. The mod allows players to mark specific chests, track their contents in real-time, and receive detailed inventories via a webhook directly to a Discord server. 

Whether you're away from your base or want to monitor your storage, **CheckYourChest** provides a seamless way to stay updated on what's happening in your chests.

---

## Features

- **Mark Any Chest**  
  Mark any single or double chest in your world and monitor its contents.
  
- **Track Single and Double Chests**  
  Automatically handle both single and double chests with ease.
  
- **Real-Time Updates**  
  Monitor chest contents in real-time and automatically send updates at specified intervals.

- **Discord Integration via Webhook**  
  Send detailed inventory reports directly to a Discord channel using a webhook.

- **Ideal for Automated Systems**  
  Perfect for automation, farms, and large storage systems where keeping track of resources is essential.

---

## How It Works

1. **Marking a Chest**  
   Use a custom item (`Marking Stick`) to mark any chest. When a chest is marked, its contents will be tracked and updated at regular intervals.

2. **Chest Monitoring**  
   Track the chest's contents in real-time. The mod works with both single and double chests, ensuring comprehensive tracking for all your storage needs.

3. **Automatic Webhook Updates**  
   At each interval (defined in the config), the contents of the marked chest(s) are sent to your designated Discord webhook, providing a detailed list of items and their quantities.

---

## Installation

1. **Requirements**
   - Minecraft Forge (Version 1.20.1 or above)
   - [Download the latest release](https://github.com/your-repo/checkyourchest/releases) of the mod

2. **Install the Mod**
   - Place the downloaded `.jar` file into your Minecraft `mods` folder.
   - Ensure you have the correct Forge version installed.

---

## Configuration

1. **Webhook Setup**  
   To set up a Discord webhook, follow these steps:
   - Go to your Discord channel settings.
   - Under the "Integrations" tab, create a new webhook and copy its URL.
   - Paste the webhook URL into the mod's configuration file (`config/checkyourchest-common.toml`).

2. **Interval Settings**  
   You can customize how often chest updates are sent to the webhook by adjusting the `checkInterval` in the config file. This value is in ticks (20 ticks = 1 second). For example:
   ```toml
   checkInterval = 12000  # Sends updates every 10 minutes (12000 ticks)

## Usage

1. **Command to Get the Marking Stick**  
   Use the `/cyc` command in the game to get the **Marking Stick**. This special item allows you to mark chests for monitoring.

2. **Marking a Chest**  
   To mark a chest, hold the **Marking Stick** and **sneak right-click** on the chest you want to track. The mod will start monitoring the contents of the chest and send updates to your webhook at the configured interval.

3. **Unmarking a Chest**  
   To unmark a chest, simply **sneak right-click** on the marked chest or any other block with the **Marking Stick**, and the chest will be unmarked.


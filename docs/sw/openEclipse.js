var uiBot = Java.type('org.docdriven.script.ui.bot.UIBot').getInstance();
uiBot.activateWindow();

// open Project
uiBot.menu('File').menu('New').menu('Project...').click();


var uiBot = new org.docdriven.script.ui.bot.UIBot();
uiBot.activateWindow();

// open Project
uiBot.getSWTBot().menu('File').menu('New').menu('Project...').click();


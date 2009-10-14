fl.outputPanel.clear();

var doc = fl.openDocument("file:///C:/smartequip/workspace/eclipse3.5/citrine-phase2/static-web-jlg/seflash-common/source/fla/integrationApplication.fla");
fl.outputPanel.trace("Publishing SWF - file:///C:/smartequip/workspace/eclipse3.5/citrine-phase2/static-web-jlg/seflash-common/target/seflash-common-1.0.0-SNAPSHOT/integrationApplication.swf");
doc.exportSWF("file:///C:/smartequip/workspace/eclipse3.5/citrine-phase2/static-web-jlg/seflash-common/target/seflash-common-1.0.0-SNAPSHOT/integrationApplication.swf", true);
fl.closeDocument(doc, false);

var doc = fl.openDocument("file:///C:/smartequip/workspace/eclipse3.5/citrine-phase2/static-web-jlg/seflash-common/source/domainInit/domainInit.fla");
fl.outputPanel.trace("Publishing SWF - file:///C:/smartequip/workspace/eclipse3.5/citrine-phase2/static-web-jlg/seflash-common/target/seflash-common-1.0.0-SNAPSHOT/domainInit.swf");
doc.exportSWF("file:///C:/smartequip/workspace/eclipse3.5/citrine-phase2/static-web-jlg/seflash-common/target/seflash-common-1.0.0-SNAPSHOT/domainInit.swf", true);
fl.closeDocument(doc, false);

fl.compilerErrors.save("file:///C:/smartequip/workspace/eclipse3.5/citrine-phase2/static-web-jlg/seflash-common/target/trace.log",true);
var errorLog = FLfile.read("file:///C:/smartequip/workspace/eclipse3.5/citrine-phase2/static-web-jlg/seflash-common/target/trace.log");
if (errorLog.length < 1){   FLfile.remove("file:///C:/smartequip/workspace/eclipse3.5/citrine-phase2/static-web-jlg/seflash-common/target/trace.log"); }; 
	fl.quit(true);

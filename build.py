#!/usr/bin/env python3
"""Offline build against the exact SRG jars already installed by PrismLauncher."""
import os, pathlib, subprocess, zipfile, shutil, sys
ROOT = pathlib.Path(__file__).resolve().parent
PRISM = pathlib.Path(os.environ.get('PRISM_ROOT', pathlib.Path.home()/'.local/share/PrismLauncher'))
INSTANCE = pathlib.Path(os.environ.get('TFG_INSTANCE', PRISM/'instances/TerraFirmaGreg-Modern'))
JAVA_HOME = pathlib.Path(os.environ.get('JAVA_HOME', PRISM/'java/java-runtime-gamma'))
LIBS = PRISM/'libraries'
CORE = INSTANCE/'minecraft/mods/TerraFirmaGreg-Core-Modern-0.9.30.jar'
if not CORE.exists(): raise SystemExit('Missing exact TFG Core 0.9.30: set TFG_INSTANCE.')
jars = [LIBS/'net/minecraft/client/1.20.1-20230612.114412/client-1.20.1-20230612.114412-srg.jar', LIBS/'net/minecraftforge/forge/1.20.1-47.4.13/forge-1.20.1-47.4.13-universal.jar', CORE]
import json
for metadata in ['net.minecraftforge/47.4.13.json', 'net.minecraft/1.20.1.json', 'org.lwjgl3/3.3.1.json']:
    data=json.loads((PRISM/'meta'/metadata).read_text())
    for library in data.get('libraries',[]):
        artifact=library.get('downloads',{}).get('artifact',{})
        parts=library['name'].split(':')
        group, name, version=parts[:3]
        suffix=('-'+parts[3]) if len(parts)>3 else ''
        rel=artifact.get('path', group.replace('.','/')+'/'+name+'/'+version+'/'+name+'-'+version+suffix+'.jar')
        p=LIBS/rel
        if p.exists() and p not in jars: jars.append(p)
for p in sorted((LIBS/'net/minecraftforge').glob('*/1.20.1-47.4.13/*.jar')):
    if p.name.endswith(('-installer.jar','-client.jar')): continue
    if p not in jars: jars.append(p)
CP = os.pathsep.join(map(str,jars))
(ROOT/'build').mkdir(exist_ok=True)
(ROOT/'build/classpath.txt').write_text(CP)

def compile_set(destination, sources, cp):
    destination.mkdir(parents=True,exist_ok=True)
    argfile=ROOT/'build/javac.args'
    args=['--release','17','-encoding','UTF-8','-proc:none','-classpath',cp,'-d',str(destination)]+list(map(str,sources))
    argfile.write_text('\n'.join('"'+a.replace('\\','\\\\').replace('"','\\"')+'"' for a in args))
    subprocess.run([str(JAVA_HOME/'bin/javac'),'@'+str(argfile)],check=True)

def run_test(name, cp, *arguments):
    result=subprocess.run([str(JAVA_HOME/'bin/java'),'-Xmx512m','-cp',cp,name,*map(str,arguments)])
    if result.returncode: raise SystemExit('FAILED '+name+' (exit '+str(result.returncode)+')')

if __name__=='__main__':
    main=ROOT/'build/classes'; test=ROOT/'build/test'
    compile_set(main, sorted((ROOT/'src/main/java').rglob('*.java')), CP)
    compile_set(test, sorted((ROOT/'src/test/java').rglob('*.java')),str(main)+os.pathsep+CP)
    for p in sorted((ROOT/'src/test/java').rglob('*Test.java')):
        run_test('.'.join(p.relative_to(ROOT/'src/test/java').with_suffix('').parts),str(test)+os.pathsep+str(main)+os.pathsep+CP)
    out=ROOT/'dist/tfg-anvil-helper-1.1.2-mc1.20.1.jar';out.parent.mkdir(exist_ok=True)
    with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as z:
        for base in [main,ROOT/'src/main/resources']:
            if base.exists():
                for p in sorted(base.rglob('*')):
                    if p.is_file():z.write(p,str(p.relative_to(base)))
        for name in ['LICENSE','NOTICE']:
            p=ROOT/name
            if p.exists():z.write(p,name)
    run_test('vn.bdtg.anvil.ResourcePackProbe',str(test)+os.pathsep+str(main)+os.pathsep+CP,out)
    print('BUILT',out, out.stat().st_size,'bytes')

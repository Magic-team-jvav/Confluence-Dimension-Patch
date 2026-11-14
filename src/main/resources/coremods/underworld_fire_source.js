// Coremod replacement for mixin.underworld.IBlockExtensionMixin.
// Forge/Mixin cannot inject into interface methods, so the default method of
// net.minecraftforge.common.extensions.IForgeBlock is patched with ASM instead.
//
// Injected equivalent (this method is a default interface method, so slot 0 is `this`):
//   if (CDPCoreModHooks.isUnderworld(level, pos))
//       return CDPCoreModHooks.isNetherInfiniburn(state);

var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');

var HOOKS = 'org/mesdag/confluence_dimension_patch/coremod/CDPCoreModHooks';
var BLOCK_STATE = 'Lnet/minecraft/world/level/block/state/BlockState;';
var LEVEL_READER = 'Lnet/minecraft/world/level/LevelReader;';
var BLOCK_POS = 'Lnet/minecraft/core/BlockPos;';
var DIRECTION = 'Lnet/minecraft/core/Direction;';

function initializeCoreMod() {
    return {
        'underworld_fire_source': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraftforge.common.extensions.IForgeBlock',
                'methodName': 'isFireSource',
                'methodDesc': '(' + BLOCK_STATE + LEVEL_READER + BLOCK_POS + DIRECTION + ')Z'
            },
            'transformer': function (method) {
                var skipInjection = new LabelNode();
                var instructions = new InsnList();

                instructions.add(new VarInsnNode(Opcodes.ALOAD, 2)); // level
                instructions.add(new VarInsnNode(Opcodes.ALOAD, 3)); // pos
                instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, 'isUnderworld',
                    '(' + LEVEL_READER + BLOCK_POS + ')Z', false));
                instructions.add(new JumpInsnNode(Opcodes.IFEQ, skipInjection));

                instructions.add(new VarInsnNode(Opcodes.ALOAD, 1)); // state
                instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, 'isNetherInfiniburn',
                    '(' + BLOCK_STATE + ')Z', false));
                instructions.add(new InsnNode(Opcodes.IRETURN));

                instructions.add(skipInjection);

                method.instructions.insert(instructions);
                ASMAPI.log('DEBUG', 'Injected underworld fire source check into IForgeBlock#isFireSource');
                return method;
            }
        }
    };
}

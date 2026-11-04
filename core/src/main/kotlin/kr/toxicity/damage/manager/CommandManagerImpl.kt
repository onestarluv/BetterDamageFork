package kr.toxicity.damage.manager

import kr.toxicity.damage.api.manager.CommandManager
import kr.toxicity.damage.api.pack.PackAssets
import kr.toxicity.damage.gui.SkinMenu
import kr.toxicity.damage.util.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.CloudBukkitCapabilities
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.LegacyPaperCommandManager
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.suggestion.SuggestionProvider.blockingStrings
import org.incendo.cloud.exception.*

object CommandManagerImpl : CommandManager {
    override fun reload(assets: PackAssets) {}

    override fun start() {
        SkinMenu.register()
        val manager = LegacyPaperCommandManager.createNative(PLUGIN, ExecutionCoordinator.simpleCoordinator()).apply {
            exceptionController().apply {
                registerHandler(NoPermissionException::class.java) { ctx ->
                    ctx.context().sender().audience().warn("명령어를 사용할 권한이 없습니다.")
                }
                registerHandler(NoSuchCommandException::class.java) { ctx ->
                    ctx.context().sender().audience().warn("존재하지 않는 명령어입니다. /데미지스킨을 사용해주세요.")
                }
                registerHandler(InvalidCommandSenderException::class.java) { ctx ->
                    ctx.context().sender().audience().warn("게임 내 플레이어만 사용할 수 있습니다.")
                }
                registerHandler(InvalidSyntaxException::class.java) { ctx ->
                    ctx.context().sender().audience().warn("사용법: /${ctx.exception().correctSyntax()}")
                }
                registerHandler(ArgumentParseException::class.java) { ctx ->
                    ctx.context().sender().audience().warn("입력값을 확인해주세요.")
                }
                registerHandler(CommandExecutionException::class.java) { ctx ->
                    ctx.exception().handle("명령어 처리 중 오류가 발생했습니다.")
                    ctx.context().sender().audience().warn("명령어 처리 중 오류가 발생했습니다.")
                }
                registerHandler(Throwable::class.java) { ctx ->
                    ctx.exception().handle("명령어 처리 중 오류가 발생했습니다.")
                    ctx.context().sender().audience().warn("명령어 처리 중 오류가 발생했습니다.")
                }
            }
            if (hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) registerBrigadier()
            else if (hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) registerAsynchronousCompletions()
        }
        val root = manager.commandBuilder("데미지스킨", "bd", "betterdamage")
        manager.command(root.permission("betterdamage.gui").senderType(Player::class.java).handler { ctx ->
            ctx.sender().onPlayerThread { SkinMenu.open(ctx.sender()) }
        })
        manager.command(root.literal("장착").permission("betterdamage.select")
            .required("플레이어", stringParser(), SkinPlayers.suggestions())
            .required("스킨", stringParser(), blockingStrings { _, _ -> DamageSkinManagerImpl.displayNames() })
            .handler { ctx ->
                val name = ctx.get<String>("스킨")
                withTarget(ctx.sender(), ctx.get("플레이어")) { player ->
                    val skin = DamageSkinManagerImpl.skin(name)
                    val data = PlayerManagerImpl.player(player.uniqueId)?.data()
                    when {
                        skin == null -> ctx.sender().audience().warn("존재하지 않는 스킨입니다: $name")
                        data == null -> ctx.sender().audience().warn("플레이어 데이터를 불러오는 중입니다.")
                        data.select(skin) -> ctx.sender().audience().info("${player.name}님에게 ${skin.displayName()} 데미지 스킨을 장착했습니다.")
                        else -> ctx.sender().audience().warn("대상에게 해당 스킨 권한이 없습니다.")
                    }
                }
            })
    }

    private fun withTarget(sender: CommandSender, identifier: String, action: (Player) -> Unit) {
        async {
            runCatching {
                val target = SkinPlayers.find(identifier)
                if (target == null) sender.audience().warn("접속 중인 플레이어를 찾을 수 없습니다: $identifier")
                else target.onPlayerThread { if (target.isOnline) action(target) }
            }.onFailure { it.handle(sender, "플레이어 별명 조회에 실패했습니다.") }
        }
    }
}

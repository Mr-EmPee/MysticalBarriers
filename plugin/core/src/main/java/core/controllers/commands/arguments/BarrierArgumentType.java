package core.controllers.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import core.exceptions.PluginException;
import core.model.Barrier;
import core.services.BarriersService;
import io.github.empee.colonel.arguments.CustomArgumentType;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "barrier")
public class BarrierArgumentType implements CustomArgumentType<Barrier> {

  private final BarriersService barriersService;

  public Barrier parse(StringReader reader) throws CommandSyntaxException {
    return barriersService.findById(reader.readString()).orElseThrow(
        () -> PluginException.of("barrier_not_found")
    );
  }

  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
    barriersService.findAll().forEach(
        b -> builder.suggest(b.getId())
    );

    return builder.buildFuture();
  }

  @Override
  public ArgumentType<?> getNmsType() {
    return StringArgumentType.string();
  }
}

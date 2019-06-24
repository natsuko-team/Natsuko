package ninja.natsuko.bot.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * Marks a command as invisible.
 */
@Retention(RetentionPolicy.RUNTIME) // keep at runtime so we can access in reflection
@Target(ElementType.TYPE) // restrict to classes
public @interface Invisible {}
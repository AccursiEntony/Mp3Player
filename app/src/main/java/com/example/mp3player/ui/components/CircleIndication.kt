package com.example.mp3player.ui.components

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun rememberCircleIndication(color: Color): IndicationNodeFactory {
    return remember(color) { CircleIndication(color) }
}

private class CircleIndication(private val color: Color) : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return CircleIndicationNode(color, interactionSource)
    }

    // Obbligatorio per IndicationNodeFactory: serve a Compose per riutilizzare l'istanza
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CircleIndication) return false
        return color == other.color
    }

    override fun hashCode(): Int {
        return color.hashCode()
    }

    // Rendi la classe interna (inner) per accedere a 'coroutineScope' e 'invalidateDraw' del nodo
    private inner class CircleIndicationNode(
        private val color: Color,
        private val interactionSource: InteractionSource
    ) : Modifier.Node(), DrawModifierNode { // Estendi Modifier.Node anziché Animatable

        private var isActive = false

        override fun onAttach() {
            super.onAttach()
            // coroutineScope è ora accessibile perché erediti da Modifier.Node
            coroutineScope.launch {
                interactionSource.interactions.collectLatest { interaction ->
                    isActive = when (interaction) {
                        is PressInteraction.Press,
                        is HoverInteraction.Enter,
                        is FocusInteraction.Focus -> true
                        // Gestisci il rilascio o l'uscita dell'interazione
                        is PressInteraction.Release, is PressInteraction.Cancel,
                        is HoverInteraction.Exit,
                        is FocusInteraction.Unfocus -> false
                        else -> isActive // Mantiene lo stato per altre interazioni estranee
                    }
                    invalidateDraw() // Ora importato correttamente
                }
            }
        }

        override fun ContentDrawScope.draw() {
            drawContent()
            if (isActive) {
                drawCircle(
                    color = color.copy(alpha = 0.18f),
                    radius = size.minDimension / 2f
                )
            }
        }
    }
}

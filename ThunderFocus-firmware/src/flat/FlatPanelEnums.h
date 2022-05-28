#ifndef FLAT_PANEL_ENUMS_H
#define FLAT_PANEL_ENUMS_H

namespace FlatPanel {
enum MotorDirection { OPENING = 0, CLOSING, NONE };
enum CoverStatus { NEITHER_OPEN_NOR_CLOSED = 0, CLOSED, OPEN, HALT };
}  // namespace FlatPanel

#endif
package com.glpalma.HomeTreasury.pluggy;

import com.glpalma.HomeTreasury.home.Home;
import com.glpalma.HomeTreasury.home.PluggyItem;
import com.glpalma.HomeTreasury.home.PluggyItemRepository;
import com.glpalma.HomeTreasury.user.CurrentMembershipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/api/pluggy")
public class PluggyController {

    private final PluggyClient pluggyClient;
    private final CurrentMembershipService memberships;
    private final PluggyItemRepository pluggyItems;

    public PluggyController(
            PluggyClient pluggyClient,
            CurrentMembershipService memberships,
            PluggyItemRepository pluggyItems
    ) {
        this.pluggyClient = pluggyClient;
        this.memberships = memberships;
        this.pluggyItems = pluggyItems;
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/connect-token")
    public PluggyConnectTokenResponse connectToken() {
        return pluggyClient.getConnectToken();
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/item")
    @ResponseStatus(HttpStatus.CREATED)
    public PluggyItemResponse linkItem(
            @Valid @RequestBody LinkPluggyItemRequest request,
            Authentication authentication
    ) {
        Home home = memberships.require(authentication).home();
        if (pluggyItems.findByHome(home).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Home already has a linked Pluggy item");
        }

        PluggyItemDetails details;
        try {
            details = pluggyClient.getItem(request.itemId());
        } catch (HttpClientErrorException.NotFound | HttpClientErrorException.BadRequest ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown Pluggy item");
        }

        PluggyItem saved = pluggyItems.save(new PluggyItem(
                home,
                details.id(),
                details.connector() == null ? null : details.connector().name(),
                Instant.now()
        ));
        return PluggyItemResponse.from(saved);
    }

    @GetMapping("/item")
    public PluggyItemResponse getItem(Authentication authentication) {
        Home home = memberships.require(authentication).home();
        PluggyItem item = pluggyItems.findByHome(home)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No Pluggy item linked to this home"));
        return PluggyItemResponse.from(item);
    }
}
